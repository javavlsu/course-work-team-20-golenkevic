package ru.vlsu.marketplace.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.vlsu.marketplace.entities.ExperimentLog;
import ru.vlsu.marketplace.repositories.ExperimentLogRepository;

import java.time.Instant;

/**
 * Логи экспериментов пишем строго в отдельной транзакции
 * ({@link Propagation#REQUIRES_NEW}), чтобы они не откатывались
 * вместе с откатом основной транзакции эксперимента.
 * <p>
 * Метод вынесен в отдельный @Service умышленно: при вызове через self
 * (this.writeLog) Spring AOP-прокси не перехватывает вызов и аннотация
 * {@link Transactional} игнорируется. Только вызовы через бин-прокси
 * (autowired в другой класс) проходят через транзакционный advice.
 */
@Service
@RequiredArgsConstructor
public class ExperimentLogger {

    private final ExperimentLogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeLog(String experiment, String method,
                         ExperimentLog.Status status, String message) {
        ExperimentLog entry = new ExperimentLog();
        entry.setExperiment(experiment);
        entry.setMethodName(method);
        entry.setStatus(status);
        entry.setMessage(message);
        entry.setCreatedAt(Instant.now());
        logRepository.save(entry);
    }
}
