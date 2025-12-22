package com.ejemplo.batch.config;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * TransactionManager dinámico que obtiene el TransactionManager real
 * del ThreadLocal cuando se necesita, permitiendo crear y destruir
 * componentes de base de datos para cada ejecución.
 */
public class DynamicTransactionManager implements PlatformTransactionManager {

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        PlatformTransactionManager tm = getCurrentTransactionManager();
        if (tm == null) {
            throw new IllegalStateException("TransactionManager no está disponible. Asegúrate de que DatabaseLifecycleManager.beforeStep() se ejecutó primero.");
        }
        return tm.getTransaction(definition);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        PlatformTransactionManager tm = getCurrentTransactionManager();
        if (tm == null) {
            throw new IllegalStateException("TransactionManager no está disponible.");
        }
        tm.commit(status);
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        PlatformTransactionManager tm = getCurrentTransactionManager();
        if (tm != null) {
            tm.rollback(status);
        }
    }

    private PlatformTransactionManager getCurrentTransactionManager() {
        return com.ejemplo.batch.processor.DatabaseLifecycleManager.getCurrentTransactionManager();
    }
}

