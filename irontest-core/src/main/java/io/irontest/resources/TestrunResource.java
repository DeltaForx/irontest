package io.irontest.resources;

import io.irontest.core.assertion.AssertionVerifier;
import io.irontest.core.assertion.AssertionVerifierFactory;
import io.irontest.core.runner.TeststepRunnerFactory;
import io.irontest.db.TeststepDAO;
import io.irontest.db.UtilsDAO;
import io.irontest.models.Endpoint;
import io.irontest.models.Testrun;
import io.irontest.models.Teststep;
import io.irontest.models.assertion.Assertion;
import io.irontest.models.assertion.AssertionVerification;
import io.irontest.models.assertion.AssertionVerificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by Trevor Li on 24/07/2015.
 */
@Path("/testruns") @Produces({ MediaType.APPLICATION_JSON })
public class TestrunResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestrunResource.class);
    private final TeststepDAO teststepDao;
    private final UtilsDAO utilsDAO;

    public TestrunResource(TeststepDAO teststepDao, UtilsDAO utilsDAO) {
        this.teststepDao = teststepDao;
        this.utilsDAO = utilsDAO;
    }

    private Object runTeststep(Teststep teststep) throws Exception {
        Endpoint endpoint = teststep.getEndpoint();
        if (endpoint != null && endpoint.getPassword() != null) {
            endpoint.setPassword(utilsDAO.decryptPassword(endpoint.getPassword()));
        }
        return TeststepRunnerFactory.getInstance().getTeststepRunner(teststep.getType() + "TeststepRunner")
                .run(teststep);
    }

    @POST
    public Testrun create(Testrun testrun) throws Exception {
        if (testrun.getTeststep() != null) {  //  run a test step (passing invocation response back to client)
            LOGGER.info("Running an individual test step.");
            testrun.setResponse(runTeststep(testrun.getTeststep()));
            testrun.setTeststep(null);    //  no need to pass the test step back to client which might contain decrypted password
        } else if (testrun.getTestcaseId() != null) {  //  run a test case (not passing invocation responses back to client)
            LOGGER.info("Running a test case.");
            List<Teststep> teststeps = teststepDao.findByTestcaseId(testrun.getTestcaseId());

            for (Teststep teststep : teststeps) {
                //  run and get response
                Object response = runTeststep(teststep);
                LOGGER.info(response == null ? null : response.toString());

                //  verify assertions against the invocation response
                for (Assertion assertion : teststep.getAssertions()) {
                    AssertionVerification verification = new AssertionVerification();
                    verification.setAssertion(assertion);
                    verification.setInput(response);
                    AssertionVerifier verifier = new AssertionVerifierFactory().create(assertion.getType());
                    AssertionVerificationResult verificationResult = verifier.verify(verification);
                    if (Boolean.FALSE == verificationResult.getPassed()) {
                        testrun.getFailedTeststepIds().add(teststep.getId());
                        break;
                    }
                }
            }
        }

        return testrun;
    }

    @DELETE @Path("{testrunId}")
    public void delete(@PathParam("testrunId") long testrunId) {
    }

    @GET
    public List<Testrun> findAll() {
        return null;
    }

    @GET @Path("{testrunId}")
    public Testrun findById(@PathParam("testrunId") long testrunId) {
        return null;
    }
}