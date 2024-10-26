package corp.base;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherFactory;

import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AllTestsLauncher {
    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        DiscoverySelectors.selectClass(NoteControllerTest.class),
                        DiscoverySelectors.selectClass(NoteServiceTest.class),
                        DiscoverySelectors.selectClass(UserServiceTest.class)
                )
                .build();

        Launcher launcher = LauncherFactory.create();

        SummaryGeneratingListener listenerFailures = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listenerFailures);

        CustomTestExecutionListener listenerSuccess = new CustomTestExecutionListener();
        launcher.registerTestExecutionListeners(listenerSuccess);

        launcher.execute(request);

        TestExecutionSummary summary = listenerFailures.getSummary();
        summary.printTo(new PrintWriter(System.out));

        System.out.println("Results:");
        System.out.printf("Total tests: %d%n", summary.getTestsStartedCount());
        System.out.printf("Passed tests: %d%n", summary.getTestsSucceededCount());
        System.out.printf("Failed tests: %d%n", summary.getTestsFailedCount());

        if (summary.getTestsFailedCount() > 0) {
            System.out.println("Failed tests details:");
            summary.getFailures().forEach(failure -> {
                System.out.printf("Test %s failed: %s%n",
                        failure.getTestIdentifier().getDisplayName(),
                        failure.getException().getMessage());
            });
        }

        System.out.println("\n");
        System.out.println("Successfully completed tests:");
        listenerSuccess.getSuccessfulTests().forEach(test -> {
            System.out.printf("The test named %s has been successfully completed.%n",
                    test.getDisplayName());
            System.out.println("Details: " + test.getSource());
        });
    }

    static class CustomTestExecutionListener implements TestExecutionListener {
        private final List<TestIdentifier> successfulTests = new ArrayList<>();

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            if (testExecutionResult.getStatus() == TestExecutionResult.Status.SUCCESSFUL) {
                successfulTests.add(testIdentifier);
            }
        }

        public List<TestIdentifier> getSuccessfulTests() {
            return successfulTests;
        }
    }
}

