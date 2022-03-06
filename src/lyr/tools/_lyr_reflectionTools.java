package lyr.tools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Provides MethodHandles and a few methods that will be useful
 * where reflection package access is restricted. MethodHandles
 * are used to go around the restriction.
 * <p> These can be used anywhere, however if there's no such
 * restriction on the reflection package, then there is no 
 * point in using this. 
 * @author lyravega
 */
public class _lyr_reflectionTools {
	protected static final Logger logger = Logger.getLogger("lyr_reflectionTools");
	protected static final Lookup lookup = MethodHandles.lookup();
	protected static Class<?> fieldClass;
	protected static Class<?> methodClass;
	protected static MethodHandle getName;
	protected static MethodHandle getParameterTypes;
	protected static MethodHandle getReturnType;
	protected static MethodHandle getDeclaredMethod;
	protected static MethodHandle getMethod;

	static {
		logger.setLevel(Level.INFO);
		try {
			fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
			methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
			getName = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));
			getParameterTypes = lookup.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(Class[].class));
			getReturnType = lookup.findVirtual(methodClass, "getReturnType", MethodType.methodType(Class.class));
			getDeclaredMethod = lookup.findVirtual(Class.class, "getDeclaredMethod", MethodType.methodType(methodClass, String.class, Class[].class));
			getMethod = lookup.findVirtual(Class.class, "getMethod", MethodType.methodType(methodClass, String.class, Class[].class));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	protected static class methodMap {
		private Class<?> returnType;
		private Class<?>[] parameterTypes;
		private MethodType methodType;
		private MethodHandle methodHandle;

		protected methodMap(Class<?> returnType, Class<?>[] parameterTypes, MethodType methodType, MethodHandle methodHandle) {
			this.returnType = returnType;
			this.parameterTypes = parameterTypes;
			this.methodType = methodType;
			this.methodHandle = methodHandle;
		}

		protected Class<?> getReturnType() { return this.returnType; }
		protected Class<?>[] getParameterTypes() { return this.parameterTypes; }
		protected MethodType getMethodType() { return this.methodType; }
		protected MethodHandle getMethodHandle() { return this.methodHandle; }
	}

	/**
	 * Pass a class, and a methodName as a string to get the returnType and
	 * parameterTypes returned, alonside a MethodType and a ready-to-use 
	 * MethodHandle to use the said method.  
	 * @param clazz to search the methodName on
	 * @param methodName in String, no "()"
	 * @param checkDeclared overload parameter, pass false to search inherited methods as well
	 * @return a map with "returnType", "parameterTypes", "methodType" and "methodHandle" keys
	 * @throws Throwable 
	 */
	public static methodMap findTypesForMethod(Class<?> clazz, String methodName) throws Throwable {
		return findTypesForMethod(clazz, methodName, true);
	}
	public static methodMap findTypesForMethod(Class<?> clazz, String methodName, boolean checkDeclared) throws Throwable {
		Object method = null;
		try { // works for methods with no arguments; if there's a way to capture it through a MethodHandle, I'm not aware of it
			method = (checkDeclared) ? getDeclaredMethod.invoke(clazz, methodName) : getMethod.invoke(clazz, methodName);
		} catch (Throwable t) { // fallback for the problem above, searches all the methods with the passed name, and uses the FIRST found one
			for (Object currMethod : (checkDeclared) ? clazz.getDeclaredMethods() : clazz.getMethods()) {
				if (!String.class.cast(getName.invoke(currMethod)).equals(methodName)) continue;

				method = currMethod; break;
			}
		}

		if (method == null) throw new Throwable("Declared method with the name '"+methodName+"' not found in the class '"+clazz.getName()+"'");

		Class<?> returnType = (Class<?>) getReturnType.invoke(method);
		Class<?>[] parameterTypes = (Class<?>[]) getParameterTypes.invoke(method);

		MethodType methodType = MethodType.methodType(returnType, parameterTypes);
		MethodHandle methodHandle = MethodHandles.lookup().findVirtual(clazz, methodName, MethodType.methodType(returnType, parameterTypes));

		return new methodMap(returnType, parameterTypes, methodType, methodHandle);
	}
}
