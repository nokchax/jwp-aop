package core.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ExceptionHandler {
    ModelAndView handle(Throwable exception, HttpServletRequest request, HttpServletResponse response);
}
