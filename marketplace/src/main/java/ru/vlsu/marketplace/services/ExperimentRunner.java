package ru.vlsu.marketplace.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Три эксперимента из ЛР5.
 * <p>
 * exp1 / exp2 объявлены {@code @Transactional} — обе пары вызовов me1+me2
 * выполняются в одной транзакции.
 * <p>
 * exp3 НЕ имеет аннотации — каждый вызов me1/me2 идёт в собственной короткой
 * транзакции, открытой Hibernate'ом для save().
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExperimentRunner {

    private final ExperimentService experimentService;

    /** Эксперимент 1: одна транзакция, обе операции успешны. */
    @Transactional
    public void exp1() {
        log.info("=== exp1: TRANSACTIONAL, both succeed ===");
        experimentService.me1("exp1");
        experimentService.me2("exp1", false);
    }

    /** Эксперимент 2: одна транзакция, me2 падает. Откат должен забрать и me1. */
    @Transactional
    public void exp2() {
        log.info("=== exp2: TRANSACTIONAL, me2 fails -> rollback ===");
        experimentService.me1("exp2");
        experimentService.me2("exp2", true);
    }

    /** Эксперимент 3: БЕЗ транзакции. me1 уже зафиксировался к моменту падения me2. */
    public void exp3() {
        log.info("=== exp3: NO TRANSACTION, me2 fails -> me1 stays ===");
        experimentService.me1("exp3");
        experimentService.me2("exp3", true);
    }
}
