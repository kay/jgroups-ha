package org.neverfear.ha;

public interface LeadershipListener {
	void onAcquired();

	void onRelinquished();
}
