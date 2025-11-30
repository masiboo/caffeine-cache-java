package nl.ing.api.contacting.conf.repository.cassandra;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CassandraInMemoryTestConfiguration.class})
public abstract class CassandraInMemoryTest {

}
