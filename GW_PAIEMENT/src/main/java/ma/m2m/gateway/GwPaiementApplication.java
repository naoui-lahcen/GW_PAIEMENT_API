package ma.m2m.gateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@SpringBootApplication
@ComponentScan("ma.m2m.gateway.*")
@EnableJpaRepositories("ma.m2m.gateway.repository")
public class GwPaiementApplication 
{
	
	public static void main(String[] args) 
		{
	
			SpringApplication.run(GwPaiementApplication.class, args);

		}
}