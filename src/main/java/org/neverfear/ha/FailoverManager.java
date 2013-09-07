package org.neverfear.ha;

import java.io.InputStream;
import java.io.OutputStream;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelListener;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author doug@neverfear.org
 * 
 */
public final class FailoverManager
	implements AutoCloseable, ChannelListener, Receiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(FailoverManager.class);

	private static final String HA_CLUSTER = "ha-cluster";

	private final JChannel channel;
	private final int maximumClusterSize;

	private final LeadershipResolver decider;

	public FailoverManager(final String configName,
			final String applicationGroup,
			final int maximumClusterSize,
			final LeadershipListener leadershipListener)
			throws HighAvailabilityException {
		if (maximumClusterSize < 3) {
			throw new IllegalArgumentException("Cluster must contain at least 3 members");
		}

		this.maximumClusterSize = maximumClusterSize;

		try {
			this.channel = new JChannel(configName);
			this.channel.addChannelListener(this);
			this.channel.setReceiver(this);

			this.decider = new LeadershipResolver(this.channel, applicationGroup, leadershipListener);

			this.channel.connect(HA_CLUSTER);

		} catch (final Exception e) {
			throw new HighAvailabilityException("Failed to initialise channel '" + applicationGroup
					+ "' and connect to '" + HA_CLUSTER + "'", e);
		}
	}

	private boolean isMajority(final int currentClusterSize) {
		final int majorityThreshold = (this.maximumClusterSize / 2) + 1;
		return (currentClusterSize >= majorityThreshold);
	}

	@Override
	public synchronized void viewAccepted(final View newView) {
		LOGGER.info("View accepted {}", newView);

		if (isMajority(newView.getMembers().size())) {
			this.decider.tryLeadership();
		} else {
			this.decider.surrenderLeadership();
		}
	}

	@Override
	public void close() {
		this.channel.close();
		this.decider.close();
	}

	@Override
	public void receive(final Message msg) {
		LOGGER.info("Received {}", msg);
	}

	@Override
	public void getState(final OutputStream output) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setState(final InputStream input) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void channelConnected(final Channel channel) {
		LOGGER.info("Channel connected {}", channel);
	}

	@Override
	public void channelDisconnected(final Channel channel) {
		LOGGER.info("Channel disconnected {}", channel);
	}

	@Override
	public void channelClosed(final Channel channel) {
		LOGGER.info("Channel closed {}", channel);
	}

	@Override
	public void suspect(final Address suspected_mbr) {
		LOGGER.debug("Suspecting {}", suspected_mbr);
	}

	@Override
	public void block() {}

	@Override
	public void unblock() {}
}
