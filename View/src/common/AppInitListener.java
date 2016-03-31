package common;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppInitListener implements ServletContextListener{
    public AppInitListener() {
        super();
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println(".......................................................");
        System.out.println("....................Application Start..................");
        System.out.println(".......................................................");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("......................................................");
        System.out.println("....................Application Stop..................");
        System.out.println("......................................................");
    }
}
