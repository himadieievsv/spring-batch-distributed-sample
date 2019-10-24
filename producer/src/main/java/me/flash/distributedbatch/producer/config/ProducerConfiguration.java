package me.flash.distributedbatch.producer.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.ChunkMessageChannelItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import java.util.Objects;

import static org.springframework.batch.item.file.transform.DelimitedLineTokenizer.DELIMITER_TAB;

@Configuration
public class ProducerConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    /**
     * Define a producer job.
     *
     * @return instance of {@link Job}.
     */
    @Bean
    public Job producerJob() {
        return this.jobBuilderFactory.get("producerJob")
                .start(readPrimesStep())
                .build();
    }

    /**
     * Define a producer step for the producer job.
     *
     * @return Instance of {@link TaskletStep}.
     */
    @Bean
    public TaskletStep readPrimesStep() {
        return this.stepBuilderFactory.get("readPrimesStep")
                .<Long, Long>chunk(10)
                .reader(primesReader(null))
                .writer(itemWriter())
                .build();
    }

    /**
     * Step scoped item reader for the producer step.
     *
     * @param resource The file is passed throw configuration (primesFile=...).
     * @return Item Reader {@link FlatFileItemReader}.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<Long> primesReader(
            @Value("#{jobParameters['pathToFile']}") Resource resource) {
        if (Objects.isNull(resource)) {
            return null;
        }
        return new FlatFileItemReaderBuilder<Long>()
                .saveState(false)
                .resource(resource)
                .fieldSetMapper(mapper -> mapper.readLong(0))
                .lineTokenizer(new DelimitedLineTokenizer(DELIMITER_TAB))
                .build();
    }

    /**
     * Step scoped item writer for the producer step.
     *
     * @return Item writer {@link ChunkMessageChannelItemWriter}.
     */
    @Bean
    @StepScope
    public ChunkMessageChannelItemWriter<Long> itemWriter() {
        ChunkMessageChannelItemWriter<Long> chunkMessageChannelItemWriter =
                new ChunkMessageChannelItemWriter<>();
        chunkMessageChannelItemWriter.setMessagingOperations(messagingTemplate());
        chunkMessageChannelItemWriter.setReplyChannel(replies());
        return chunkMessageChannelItemWriter;
    }

    /**
     * Message template for outbound adapter.
     *
     * @return Configured {@link MessagingTemplate} to use "requests" query.
     */
    @Bean
    public MessagingTemplate messagingTemplate() {
        MessagingTemplate template = new MessagingTemplate();
        template.setDefaultChannel(requests());
        template.setReceiveTimeout(2000);
        return template;
    }

    /**
     * Outbound channel.
     *
     * @return DirectChannel.
     */
    @Bean
    public DirectChannel requests() {
        return new DirectChannel();
    }

    /**
     * Creates outbound flow from EIP.
     *
     * @param amqpTemplate AMQP template instance.
     * @return Instance of {@link IntegrationFlow} for outbound adapter.
     */
    @Bean
    public IntegrationFlow outboundFlow(AmqpTemplate amqpTemplate) {
        return IntegrationFlows.from(requests())
                .handle(Amqp.outboundAdapter(amqpTemplate).routingKey("requests"))
                .get();
    }

    /**
     * Inbound channel.
     *
     * @return QueueChannel.
     */
    @Bean
    public QueueChannel replies() {
        return new QueueChannel();
    }

    /**
     * Creates inbound flow from EIP.
     *
     * @param connectionFactory connection factory to AMQP broker.
     * @return Instance of {@link IntegrationFlow}  for inbound adapter.
     */
    @Bean
    public IntegrationFlow inboundFlow(ConnectionFactory connectionFactory) {
        return IntegrationFlows
                .from(Amqp.inboundAdapter(connectionFactory, "replies"))
                .channel(replies())
                .get();
    }
}
