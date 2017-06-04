package fulltextsearch;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletMaster extends HttpServlet
{

	private static final long serialVersionUID = 1L;

	public ServletMaster()
	{
		super();
	}

	@Override
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		try
		{
			dispatch(getController(this, getMethodName(request)), this, request, response);
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}
	}

	protected void dispatch(Method m, Object target, HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		m.invoke(target, new Object[] { request, response });
	}

	protected Method getController(Object target, String methodName) throws SecurityException, NoSuchMethodException
	{
		return target.getClass().getMethod(methodName, new Class[] { HttpServletRequest.class, HttpServletResponse.class });
	}

	protected String getMethodName(HttpServletRequest req)
	{
		return req.getPathInfo().substring(1);
	}
}