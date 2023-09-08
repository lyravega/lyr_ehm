package lyravega.tools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * Reflective operation tools for all such reflection-related classes as 
 * uiTools and ProxyTools.
 * <p>This package is used indirectly and will not trigger the 
 * scriptClassLoader's restriction.
 * <p> The method {@link #inspectMethod} returns information about a
 * method a ready-to-use methodHandle to invoke it.
 * <p> The innerClass {@link methodMap} stores and provides accessors
 * for this information.
 * @author lyravega
 */
@SuppressWarnings("unused")
public class lyr_reflectionTools implements lyr_logger {
	protected static final Lookup lookup = MethodHandles.lookup();
	protected static final Class<?> lookupClass = lookup.getClass();
	private static Class<?> fieldClass;
	private static MethodHandle setAccessible;	// unused
	private static MethodHandle get;	// unused
	private static Class<?> methodClass;
	private static MethodHandle getName;
	private static MethodHandle getParameterTypes;
	private static MethodHandle getReturnType;
	private static MethodHandle getModifiers;
	// private static MethodHandle getDeclaredMethod;
	// private static MethodHandle getMethod;
	private static MethodHandle unreflect;

	static {
		try {
			fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
			setAccessible = lookup.findVirtual(fieldClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
			get = lookup.findVirtual(fieldClass, "get", MethodType.methodType(Object.class, Object.class));
			methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
			getName = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));
			getParameterTypes = lookup.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(Class[].class));
			getReturnType = lookup.findVirtual(methodClass, "getReturnType", MethodType.methodType(Class.class));
			getModifiers = lookup.findVirtual(methodClass, "getModifiers", MethodType.methodType(int.class));
			// getDeclaredMethod = lookup.findVirtual(Class.class, "getDeclaredMethod", MethodType.methodType(methodClass, String.class, Class[].class));
			// getMethod = lookup.findVirtual(Class.class, "getMethod", MethodType.methodType(methodClass, String.class, Class[].class));
			unreflect = lookup.findVirtual(lookupClass, "unreflect", MethodType.methodType(MethodHandle.class, methodClass));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
			logger.fatal(logPrefix+"Failed to initialize reflection tools", e);
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
		private int methodModifiers;
		private MethodType methodType;
		private MethodHandle methodHandle;

		/* methodMap created through getting every single type by invoking method methods on the object */
		public methodMap(Class<?> returnType, Class<?>[] parameterTypes, String methodName, int methodModifiers, MethodType methodType, MethodHandle methodHandle) {
			this.returnType = returnType;
			this.parameterTypes = parameterTypes;
			this.methodName = methodName;
			this.methodModifiers = methodModifiers;
			this.methodType = methodType;
			this.methodHandle = methodHandle;
		}

		/* methodMap created through unreflect, parameterTypes has the first one dropped as it isn't a parameter */
		public methodMap(String methodName, MethodHandle methodHandle, int methodModifiers) {
			this.returnType = methodHandle.type().returnType();
			this.parameterTypes = methodHandle.type().dropParameterTypes(0, 1).parameterArray();
			this.methodName = methodName;
			this.methodModifiers = methodModifiers;
			this.methodType = methodHandle.type();
			this.methodHandle = methodHandle;
		}
		
		public Class<?> getReturnType() { return this.returnType; }
		public Class<?>[] getParameterTypes() { return this.parameterTypes; }
		public String getName() { return this.methodName; }
		public int getModifiers() { return this.methodModifiers; }
		public MethodType getMethodType() { return this.methodType; }
		public MethodHandle getMethodHandle() { return this.methodHandle; }
	}

	/**
	 * Search for a method in a class with the given {@code methodName} as a string,
	 * and get a methodMap object that contains the returnType and parameterTypes of
	 * the found method, alongside a MethodType and a ready-to-use MethodHandle to 
	 * invoke it.
	 * <p> If a method cannot be found easily, a brute-force search will be done
	 * on all of the methods of the class. {@code declaredOnly} can be used to
	 * expand the search on inherited methods as well.
	 * <p> {@code methodParameters} can be ignored, however if given alongside a 
	 * {@code methodName}, they'll be used to perform a more specific search that
	 * can also target overloaded methods.
	 * @param methodName as String, no "()"
	 * @param clazz to search the methodName on
	 * @param declaredOnly (overload, default {@code true}) to search declared only or all methods
	 * @param methodModifier (overload, default {@code null}) to search a method with a specific modifier
	 * @param parameterTypes (optional) full set of parameters, if available and needed
	 * @return {@link methodMap}
	 * @throws Throwable if such a method is cannot be found
	 */
	public static final methodMap inspectMethod(String methodName, Class<?> clazz, Class<?>... parameterTypes) throws Throwable {
		return inspectMethod(methodName, clazz, true, null, parameterTypes);
	}
	/** @see #inspectMethod(String, Class, Class...) */
	public static final methodMap inspectMethod(String methodName, Class<?> clazz, boolean declaredOnly, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
		Object method = null; // as long as methods are stored as objects and not as methods, game is okay with it

		try {
			method = (declaredOnly) ? clazz.getDeclaredMethod(methodName, parameterTypes) : clazz.getMethod(methodName, parameterTypes);
		} catch (Throwable t) { // searches all the methods with the passed name if the above fails, and uses the FIRST found one
			for (Object currMethod : (declaredOnly) ? clazz.getDeclaredMethods() : clazz.getMethods()) {
				if (!String.class.cast(getName.invoke(currMethod)).equals(methodName)) continue;
				if (methodModifier != null && methodModifier != (int) getModifiers.invoke(currMethod)) continue;
	
				method = currMethod; break;
			}
		}

		if (method == null) throw new Throwable(logPrefix+"Method with the name '"+methodName+"' was not found in the class '"+clazz.getName()+"'");

		/* original way to build a methodMap that uses method methods to gather relevant fields */
		// Class<?> returnType = (Class<?>) getReturnType.invoke(method);
		// Class<?>[] paramTypes = (Class<?>[]) getParameterTypes.invoke(method);
		// // String methodName = (String) getName.invoke(method);
		// int methodModifiers = (int) getModifiers.invoke(method);
		// MethodType methodType = MethodType.methodType(returnType, paramTypes);
		// MethodHandle methodHandle = lookup.findVirtual(clazz, methodName, methodType);
		// return new methodMap(returnType, parameterTypes, methodName, methodModifiers, methodType, methodHandle);

		/* alternative way to build a methodMap that involves using unreflect and use the methodHandle */
		int methodModifiers = (int) getModifiers.invoke(method);
		MethodHandle methodHandle = (MethodHandle) unreflect.invoke(lookup, method);
		return new methodMap(methodName, methodHandle, methodModifiers);
	}

	/**
	 * If method name is not available (due to obfuscation for example), but some
	 * other method fields such as {@code returnType} or {@code parameterTypes}
	 * can be obtained, this overload can be used to search the methods of a class
	 * for such a method. Like its sibling, returns a methodMap.
	 * <p> Depending on the supplied parameters, one or both of the types will be
	 * used in search.
	 * @param declaredOnly (overload, default {@code true})
	 * @param clazz to search the method in
	 * @param returnType of the method being searched for
	 * @param parameterTypes (optional) full set of parameters, if available and
	 * needed
	 * @return {@link methodMap}
	 * @throws Throwable if such a method is cannot be found
	 */
	public static final methodMap inspectMethod(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
		return inspectMethod(true, clazz, returnType, parameterTypes);
	}
	/** @see #inspectMethod(Class, Class, Class...) */
	public static final methodMap inspectMethod(boolean declaredOnly, Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
		Object method = null; // as long as methods are stored as objects and not as methods, game is okay with it
		String methodName = null;

		for (Object currMethod : (declaredOnly) ? clazz.getDeclaredMethods() : clazz.getMethods()) {
			if (returnType != null && !returnType.equals(getReturnType.invoke(currMethod))) continue;
			if (parameterTypes.length > 0 && !Arrays.equals((Class<?>[]) getParameterTypes.invoke(currMethod), parameterTypes)) continue;

			methodName = (String) getName.invoke(currMethod);
			method = currMethod; break;
		}

		if (method == null) throw new Throwable(logPrefix+"Method"+(returnType != null ? " with the return type'"+returnType.toString()+"'" : "")+(parameterTypes.length > 0 ? " parameter types '"+parameterTypes.toString()+"'" : "")+"was not found in the class '"+clazz.getName()+"'");

		/* alternative way to build a methodMap that involves using unreflect and use the methodHandle */
		int methodModifiers = (int) getModifiers.invoke(method);
		MethodHandle methodHandle = (MethodHandle) unreflect.invoke(lookup, method);
		return new methodMap(methodName, methodHandle, methodModifiers);
	}
}
