jgroups-ha
==========

Simple high availability component using JGroups

What this module is
===================

This module was written to accompany this article http://seekingconsistency.com/2013-05-26/simple-cheap-and-effective-high-availability/

It's purpose is to make a decision within a distributed system over which node in a hot/warm pair should become the leader.

What this module is not
=======================

It is not a complete high availability solution. 
It is not necessarily suitable as a high availability solution for systems where a split brain is acceptable.
It does not tackle the task of restricting what side effects are produced from a node within your system
Nor does it tackle the task of deduplicating messages produced by the inevitable race condition you can see when the gc has caused a node to become unavailable.
