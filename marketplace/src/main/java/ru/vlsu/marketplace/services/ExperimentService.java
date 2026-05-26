package ru.vlsu.marketplace.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vlsu.marketplace.entities.ExperimentData;
import ru.vlsu.marketplace.entities.ExperimentLog;
import ru.vlsu.marketplace.repositories.ExperimentDataRepository;

import java.time.Instant;

/**
 * me1, me2 — два метода, делающие INSERT в experiment_data.
 * me2 опционально бросает RuntimeException, чтобы показать откат.
 * <p>
 * Логирование делегировано в отдельный {@link ExperimentLogger}, у которого
 * writeLog помечен Propagation.REQUIRES_NEW — лог сохраняется даже если
 * внешняя транзакция откатилась.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExperimentService {

    private final ExperimentDataRepository dataRepository;
    private final ExperimentLogger logger;

    public void me1(String experiment) {
        ExperimentData d = new ExperimentData();
        d.setValue("from-me1 [" + experiment + "]");
        d.setCreatedAt(Instant.now());
        ExperimentData saved = dataRepository.save(d);
        logger.writeLog(experiment, "me1", ExperimentLog.Status.SUCCESS,
                "INSERT experiment_data id=" + saved.getId());
        log.info("[{}] me1 -> SUCCESS, inserted id={}", experiment, saved.getId());
    }

    public void me2(String experiment, boolean shouldFail) {
        ExperimentData d = new ExperimentData();
        d.setValue("from-me2 [" + experiment + "]");
        d.setCreatedAt(Instant.now());
        ExperimentData saved = dataRepository.save(d);
        log.info("[{}] me2 -> inserted id={}, shouldFail={}", experiment, saved.getId(), shouldFail);

        if (shouldFail) {
            logger.writeLog(experiment, "me2", ExperimentLog.Status.FAILED,
                    "INSERT id=" + saved.getId() + " then RuntimeException");
            throw new RuntimeException("Имитация ошибки в me2");
        }

        logger.writeLog(experiment, "me2", ExperimentLog.Status.SUCCESS,
                "INSERT experiment_data id=" + saved.getId());
    }
}
