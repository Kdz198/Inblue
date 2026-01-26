package fpt.org.inblue.repository.caching;

import fpt.org.inblue.model.caching.InterviewSessionRedis;
import org.springframework.data.repository.CrudRepository;

public interface InterviewSessionRedisRepository  extends CrudRepository<InterviewSessionRedis, String> {
}
