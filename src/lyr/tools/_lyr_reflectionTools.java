package lyr.tools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Provides tools for reflective operations. Reflect package is used
 * indirectly, and will not trigger the scriptClassLoader's restriction.
 * <p> The method {@link #inspectMethod} is used to obtain 
 * information about a method, and provide a ready-to-use methodHandle. 
 * <p> The innerClass {@link methodMap} is used to store the said 
 * information above in it, and provide a proper accessors for them.
 * <p> Serves as a base for all reflection-related classes, such as 
 * uiTools and proxyTools.
 * @author lyravega
 */
public class _lyr_reflectionTools {
	protected static final Logger logger = Logger.getLogger("lyr_reflectionTools");
	protected static final Lookup lookup = MethodHandles.lookup();
	protected static final Class<?> lookupClass = lookup.getClass();
	protected static Class<?> fieldClass;
	protected static Class<?> methodClass;
	protected static MethodHandle getName;
	protected static MethodHandle getParameterTypes;
	protected static MethodHandle getReturnType;
	protected static MethodHandle getDeclaredMethod;
	protected static MethodHandle getMethod;
	protected static MethodHandle unreflect;

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
			unreflect = lookup.findVirtual(lookupClass, "unreflect", MethodType.methodType(MethodHandle.class, methodClass));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
			logger.fatal("Failed to initialize reflection tools", e);
		}
	}

	/**
	 * Provides a structure with proper accessors to store and 
	 * access to the return values for the {@code inspectMethod()}.
	 */
	public static final class methodMap {
		private Class<?> returnType;
		private Class<?>[] parameterTypes;
		private String methodName;
		private MethodType methodType;
		private MethodHandle methodHandle;

		public methodMap(Class<?> returnType, Class<?>[] parameterTypes, String methodName, MethodType methodType, MethodHandle methodHandle) {
			this.returnType = returnType;
			this.parameterTypes = parameterTypes;
			this.methodName = methodName;
			this.methodType = methodType;
			this.methodHandle = methodHandle;
		}

		public Class<?> getReturnType() { return this.returnType; }
		public Class<?>[] getParameterTypes() { return this.parameterTypes; }
		public String getName() { return this.methodName; }
		public MethodType getMethodType() { return this.methodType; }
		public MethodHandle getMethodHandle() { return this.methodHandle; }
	}

	/**
	 * Search for a method in a class with the given methodName as a string, and 
	 * get a methodMap object that contains the returnType and parameterTypes of
	 * the found method, alonside a MethodType and a ready-to-use MethodHandle 
	 * to invoke it.
	 * <p> If the method has any overloads, the first found one will be used for
	 * methodMap construction. 
	 * <p> Also handles any methods with parameters through its fallback. Finding
	 * methods with parameters through methodHandles is a bit tricky, so all of
	 * the methods will be searched to find it.
	 * @param clazz to search the methodName on
	 * @param methodName in String, no "()"
	 * @param checkDeclared overload parameter, pass false to search inherited methods as well
	 * @return a methodMap
	 * @throws Throwable
	 */
	public static final methodMap inspectMethod(Class<?> clazz, String methodName) throws Throwable {
		return inspectMethod(clazz, methodName, true);
	}
	public static final methodMap inspectMethod(Class<?> clazz, String methodName, boolean declaredOnly) throws Throwable {
		Object method = null;
		try { // works for methods with no arguments; if there's a way to capture it through a MethodHandle, I'm not aware of it
			method = (declaredOnly) ? getDeclaredMethod.invoke(clazz, methodName) : getMethod.invoke(clazz, methodName);
		} catch (Throwable t) { // fallback for the problem above, searches all the methods with the passed name, and uses the FIRST found one
			for (Object currMethod : (declaredOnly) ? clazz.getDeclaredMethods() : clazz.getMethods()) {
				if (!String.class.cast(getName.invoke(currMethod)).equals(methodName)) continue;

				method = currMethod; break;
			}
		}

		if (method == null) throw new Throwable("Method with the name '"+methodName+"' not found in the class '"+clazz.getName()+"'");

		Class<?> returnType = (Class<?>) getReturnType.invoke(method);
		Class<?>[] parameterTypes = (Class<?>[]) getParameterTypes.invoke(method);
		// String methodName = (String) getName.invoke(method);
		MethodType methodType = MethodType.methodType(returnType, parameterTypes);
		MethodHandle methodHandle = lookup.findVirtual(clazz, methodName, MethodType.methodType(returnType, parameterTypes));
		// MethodHandle methodHandle = (MethodHandle) unreflect.invoke(lookup, method); // alt way to get the same methodHandle

		return new methodMap(returnType, parameterTypes, methodName, methodType, methodHandle);
	}

	/**
	 * For cases where the method objects are available and a methodMap
	 * is needed, this alternative overload can be used to store and gain
	 * access to everything that a methodMap offers.
	 * @param clazz that has the method on
	 * @param method object
	 * @return a methodMap
	 * @throws Throwable
	 */
	public static final methodMap inspectMethod(Class<?> clazz, Object method) throws Throwable {
		Class<?> returnType = (Class<?>) getReturnType.invoke(method);
		Class<?>[] parameterTypes = (Class<?>[]) getParameterTypes.invoke(method);
		String methodName = (String) getName.invoke(method);
		MethodType methodType = MethodType.methodType(returnType, parameterTypes);
		MethodHandle methodHandle = lookup.findVirtual(clazz, methodName, MethodType.methodType(returnType, parameterTypes));

		return new methodMap(returnType, parameterTypes, methodName, methodType, methodHandle);
	}

	// public static final MethodHandle findMethodHandle(boolean isStaticMethod, Class<?> clazz, String methodName, Class<?> returnType, List<Class<?>> parameterTypes) {
	// 	MethodHandle methodHandle = null;

	// 	try {
	// 		methodHandle = isStaticMethod
	// 			? lookup.findStatic(clazz, methodName, MethodType.methodType(returnType, parameterTypes))
	// 			: lookup.findVirtual(clazz, methodName, MethodType.methodType(returnType, parameterTypes));
	// 	} catch (NoSuchMethodException | IllegalAccessException e) {
	// 		// TODO Auto-generated catch block
	// 		e.printStackTrace();
	// 	}

	// 	return methodHandle;
	// }
}
