package Database.Hooks;


import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static String configPath = "src/config.json"; // Укажите явно путь по умолчанию
    private static boolean isDatabaseInitialized = false;
    public static void initialize(String path) {
        if (path != null) {
            configPath = path;
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                File configFile = new File(configPath);
                if (!configFile.exists()) {
                    throw new FileNotFoundException("Config file not found: " + configPath);
                }

                String content = new String(Files.readAllBytes(configFile.toPath()));
                JSONObject config = new JSONObject(content);

                // Первая попытка подключения к основной БД
                try {
                    sessionFactory = createSessionFactory(config, false);
                } catch (Exception e) {
                    System.err.println("Не удалось подключиться к основной БД (" + e.getMessage() + "), пробуем локальную...");
                    try {
                        JSONObject localConfig = new JSONObject();
                        localConfig.put("HOST", "localhost");
                        localConfig.put("PORT", "5432");
                        localConfig.put("DB_Global_Module", config.getString("DB_Global_Module"));
                        localConfig.put("USER", "postgres");
                        localConfig.put("PASSWORD", ""); // или ваш пароль для локальной БД

                        // Для локальной БД принудительно устанавливаем hbm2ddl.auto=update
                        localConfig.put("HBM2DDL_AUTO", "update");

                        sessionFactory = createSessionFactory(localConfig, true);
                        System.out.println("Успешно подключились к локальной БД");
                    } catch (Exception e2) {
                        System.err.println("Не удалось подключиться и к локальной БД:");
                        e2.printStackTrace();
                        throw new ExceptionInInitializerError("Failed to initialize Hibernate with both connections: " +
                                "Main DB error: " + e.getMessage() +
                                ", Local DB error: " + e2.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Ошибка инициализации Hibernate:");
                e.printStackTrace();
                throw new ExceptionInInitializerError("Failed to initialize Hibernate: " + e.getMessage());
            }
        }
        return sessionFactory;
    }

    private static SessionFactory createSessionFactory(JSONObject config, boolean isLocal) throws Exception {
        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

        Map<String, Object> settings = new HashMap<>();
        settings.put(Environment.DRIVER, "org.postgresql.Driver");

        String url = "jdbc:postgresql://" +
                config.optString("HOST", "localhost") + ":" +
                config.optString("PORT", "5432") + "/" +
                config.getString("DB_Global_Module");

        settings.put(Environment.URL, url);
        settings.put(Environment.USER, config.getString("USER"));
        settings.put(Environment.PASS, config.getString("PASSWORD"));
        settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        settings.put(Environment.SHOW_SQL, "true");
        settings.put(Environment.FORMAT_SQL, "true");  // Форматирование SQL для удобства чтения

        // Настройки для пакетной обработки (batch)
        settings.put(Environment.STATEMENT_BATCH_SIZE, "50");  // Аналог hibernate.jdbc.batch_size
        settings.put(Environment.ORDER_INSERTS, "true");       // Оптимизация порядка INSERT
        settings.put(Environment.ORDER_UPDATES, "true");       // Оптимизация порядка UPDATE
        settings.put(Environment.BATCH_VERSIONED_DATA, "true"); // Для версионированных сущностей

        // Для PostgreSQL важно добавить:
        settings.put("hibernate.jdbc.batch_size", "50");
        settings.put("hibernate.connection.rewriteBatchedStatements", "true"); // Ключевая настройка для PostgreSQL

        // Управление схемой БД
        if (config.has("HBM2DDL_AUTO")) {
            settings.put(Environment.HBM2DDL_AUTO, config.getString("HBM2DDL_AUTO"));
        } else if (!isDatabaseInitialized) {
            settings.put(Environment.HBM2DDL_AUTO, "update");
            isDatabaseInitialized = true;
        } else {
            settings.put(Environment.HBM2DDL_AUTO, "validate");
        }

        // Дополнительные настройки для диагностики
        settings.put(Environment.FAIL_ON_PAGINATION_OVER_COLLECTION_FETCH, "true");
        settings.put(Environment.USE_SQL_COMMENTS, "true");

        registryBuilder.applySettings(settings);

        try {
            StandardServiceRegistry registry = registryBuilder.build();
            MetadataSources sources = new MetadataSources(registry)
                    .addAnnotatedClass(Database.Models.Contract.class);


            Metadata metadata = sources.getMetadataBuilder().build();
            return metadata.getSessionFactoryBuilder().build();
        } catch (Exception e) {
            System.err.println("Ошибка при создании SessionFactory для URL: " + url);
            if (e.getMessage().contains("missing table")) {
                System.err.println("РЕКОМЕНДАЦИЯ: Установите hibernate.hbm2ddl.auto=update " +
                        "для автоматического создания отсутствующих таблиц");
            }
            throw e;
        }
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

}