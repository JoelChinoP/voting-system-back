package com.votes.repository.cassandra;

import com.votes.entity.cassandra.Vote;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VoteRepository extends CassandraRepository<Vote, UUID> {
}
