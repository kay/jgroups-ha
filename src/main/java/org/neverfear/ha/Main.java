package org.neverfear.ha;

public class Main {

	private static void usageAndExit() {
		System.out.format("Usage: %s applicationGroup clusterSize%n", Main.class.getCanonicalName());
		System.exit(1);
	}

	/**
	 * @param args
	 * @throws HighAvailabilityException
	 */
	public static void main(final String[] args) throws InterruptedException {
		if (args.length < 2) {
			usageAndExit();
			return;
		}

		final String applicationGroup = args[0];
		int clusterSize;
		try {
			clusterSize = Integer.parseInt(args[1]);
		} catch (final NumberFormatException e) {
			System.out.format("Failed to parse clusterSize: %s%n", e.getMessage());
			usageAndExit();
			return;
		}

		try (FailoverManager manager = new FailoverManager("ha.xml", applicationGroup, clusterSize,
			new LeadershipListener() {

				@Override
				public void onAcquired() {
					// Boot up
					// Initialise data
					// Begin processing
				}

				@Override
				public void onRelinquished() {
					// Typically caused by the loss of consensus
				}
			});) {

			// Run for 60 seconds then shut down this node
			Thread.sleep(600000);
		} catch (final Exception e) {
			e.printStackTrace(System.out);
			usageAndExit();
			return;
		}
		System.out.println("Exiting");
	}

}
