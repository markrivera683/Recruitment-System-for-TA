package com.bupt.ta.persistence;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Initializes {@link ServiceFactory} from {@code WEB-INF/data} when the web application starts.
 */
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServiceFactory factory = ServiceFactory.fromServletContext(sce.getServletContext());
            sce.getServletContext().setAttribute(ServiceFactory.SERVLET_CONTEXT_KEY, factory);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to initialize ServiceFactory", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(ServiceFactory.SERVLET_CONTEXT_KEY);
    }
}
