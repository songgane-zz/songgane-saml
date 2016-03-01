package songgane.saml2.validator;

import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLException;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.validation.ValidationException;

import songgane.saml2.metadata.IdpMetadata;
import songgane.saml2.metadata.SpMetadata;

public class ResponseValidator {
	/* do date comparisons +/- this many seconds */
	private static final int slack = (int) TimeUnit.MINUTES.toSeconds(5);

	private IdpMetadata idpConfig;
	private SpMetadata spConfig;
	private SignatureValidator sigValidator;

	/**
	 * Create a new SAMLClient, using the IdpConfig for endpoints and
	 * validation.
	 */
	public ResponseValidator(SpMetadata spConfig, IdpMetadata idpConfig) throws SAMLException {
		this.idpConfig = idpConfig;
		this.spConfig = spConfig;

		BasicCredential cred = new BasicCredential();
		cred.setEntityId(idpConfig.getEntityId());
		cred.setPublicKey(idpConfig.getCert().getPublicKey());

		sigValidator = new SignatureValidator(cred);
	}

	public void validate(Response response) throws ValidationException {
		BasicCredential cred = new BasicCredential();
		cred.setEntityId(idpConfig.getEntityId());
		cred.setPublicKey(idpConfig.getCert().getPublicKey());

		sigValidator = new SignatureValidator(cred);

		// response signature must match IdP's key, if present
		Signature sig = response.getSignature();
		if (sig != null)
			sigValidator.validate(sig);

		// response must be successful
		if (response.getStatus() == null || response.getStatus().getStatusCode() == null
				|| !(StatusCode.SUCCESS_URI.equals(response.getStatus().getStatusCode().getValue()))) {
			throw new ValidationException("Response has an unsuccessful status code");
		}

		// response destination must match ACS
		if (!spConfig.getAcs().equals(response.getDestination()))
			throw new ValidationException("Response is destined for a different endpoint");

		DateTime now = DateTime.now();

		// issue instant must be within a day
		DateTime issueInstant = response.getIssueInstant();

		if (issueInstant != null) {
			if (issueInstant.isBefore(now.minusSeconds(slack)))
				throw new ValidationException("Response IssueInstant is in the past");

			if (issueInstant.isAfter(now.plusSeconds(slack)))
				throw new ValidationException("Response IssueInstant is in the future");
		}

		for (Assertion assertion : response.getAssertions()) {

			// Assertion must be signed correctly
			if (!assertion.isSigned())
				throw new ValidationException("Assertion must be signed");

			sig = assertion.getSignature();
			sigValidator.validate(sig);

			// Assertion must contain an authnstatement
			// with an unexpired session
			if (assertion.getAuthnStatements().isEmpty()) {
				throw new ValidationException("Assertion should contain an AuthnStatement");
			}
			for (AuthnStatement as : assertion.getAuthnStatements()) {
				DateTime sessionTime = as.getSessionNotOnOrAfter();
				if (sessionTime != null) {
					DateTime exp = sessionTime.plusSeconds(slack);
					if (exp != null && (now.isEqual(exp) || now.isAfter(exp)))
						throw new ValidationException("AuthnStatement has expired");
				}
			}

			if (assertion.getConditions() == null) {
				throw new ValidationException("Assertion should contain conditions");
			}

			// Assertion IssueInstant must be within a day
			DateTime instant = assertion.getIssueInstant();
			if (instant != null) {
				if (instant.isBefore(now.minusSeconds(slack)))
					throw new ValidationException("Response IssueInstant is in the past");

				if (instant.isAfter(now.plusSeconds(slack)))
					throw new ValidationException("Response IssueInstant is in the future");
			}

			// Conditions must be met by current time
			Conditions conditions = assertion.getConditions();
			DateTime notBefore = conditions.getNotBefore();
			DateTime notOnOrAfter = conditions.getNotOnOrAfter();

			if (notBefore == null || notOnOrAfter == null)
				throw new ValidationException("Assertion conditions must have limits");

			notBefore = notBefore.minusSeconds(slack);
			notOnOrAfter = notOnOrAfter.plusSeconds(slack);

			if (now.isBefore(notBefore))
				throw new ValidationException("Assertion conditions is in the future");

			if (now.isEqual(notOnOrAfter) || now.isAfter(notOnOrAfter))
				throw new ValidationException("Assertion conditions is in the past");

			// If subjectConfirmationData is included, it must
			// have a recipient that matches ACS, with a valid
			// NotOnOrAfter
			Subject subject = assertion.getSubject();
			if (subject != null && !subject.getSubjectConfirmations().isEmpty()) {
				boolean foundRecipient = false;
				for (SubjectConfirmation sc : subject.getSubjectConfirmations()) {
					if (sc.getSubjectConfirmationData() == null)
						continue;

					SubjectConfirmationData scd = sc.getSubjectConfirmationData();
					if (scd.getNotOnOrAfter() != null) {
						DateTime chkdate = scd.getNotOnOrAfter().plusSeconds(slack);
						if (now.isEqual(chkdate) || now.isAfter(chkdate)) {
							throw new ValidationException("SubjectConfirmationData is in the past");
						}
					}

					if (spConfig.getAcs().equals(scd.getRecipient()))
						foundRecipient = true;
				}

				if (!foundRecipient)
					throw new ValidationException("No SubjectConfirmationData found for ACS");
			}

			// audience must include intended SP issuer
			if (conditions.getAudienceRestrictions().isEmpty())
				throw new ValidationException("Assertion conditions must have audience restrictions");

			// only one audience restriction supported: we can only
			// check against the single SP.
			if (conditions.getAudienceRestrictions().size() > 1)
				throw new ValidationException("Assertion contains multiple audience restrictions");

			AudienceRestriction ar = conditions.getAudienceRestrictions().get(0);

			// at least one of the audiences must match our SP
			boolean foundSP = false;
			for (Audience a : ar.getAudiences()) {
				if (spConfig.getEntityId().equals(a.getAudienceURI()))
					foundSP = true;
			}
			if (!foundSP)
				throw new ValidationException("Assertion audience does not include issuer");
		}
	}
}
