package org.neverfear.ha;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import org.jgroups.JChannel;
import org.jgroups.blocks.locking.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves who will be leader through the use of a distributed lock.
 * 
 * Should not be called from multiple threads at once (not thread safe).
 * 
 * @author doug@neverfear.org
 */
final class LeadershipResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(LeadershipResolver.class);
	private static final String LOCK_NAME_FORMAT = "ha-%s";

	private final String lockName;
	private final Lock leaderLock;
	private final LeadershipListener leadershipListener;

	private final AtomicBoolean leader = new AtomicBoolean(false);

	public LeadershipResolver(
			final JChannel channel,
			final String name,
			final LeadershipListener leadershipListener) {
		this.lockName = String.format(LOCK_NAME_FORMAT, name);
		final LockService lockService = new LockService(channel);
		this.leaderLock = lockService.getLock(this.lockName);
		this.leadershipListener = leadershipListener;
	}

	public void tryLeadership() {
		LOGGER.info("Trying for lock");
		if (this.leaderLock.tryLock()) {
			LOGGER.info("Got lock");
			setLeader(true);
		} else {
			LOGGER.info("Failed");
			setLeader(false);
		}
	}

	public void surrenderLeadership() {
		this.leaderLock.unlock();
		setLeader(false);
	}

	public void close() {
		surrenderLeadership();
	}

	public boolean isLeader() {
		return this.leader.get();
	}

	/**
	 * There's an assumption here that the caller is always coming from the same
	 * thread. Otherwise leader=false could overtake a prior leader=true.
	 * 
	 * @param leader
	 */
	private void setLeader(final boolean leader) {
		if (leader) {
			if (this.leader.compareAndSet(false, true)) {
				LOGGER.info("Acquired leadership");
				this.leadershipListener.onAcquired();
			}
		} else {
			if (this.leader.compareAndSet(true, false)) {
				LOGGER.info("Relinquishing leadership");
				this.leadershipListener.onRelinquished();
			}
		}
	}
}
