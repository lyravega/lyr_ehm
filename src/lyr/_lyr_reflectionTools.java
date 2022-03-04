package lyr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class _lyr_reflectionTools {
	protected static final Logger logger = Logger.getLogger("lyr");
	protected static final Lookup lookup = MethodHandles.lookup();
	protected static Class<?> fieldClass;
	protected static Class<?> methodClass;
	protected static MethodHandle getName;
	protected static MethodHandle getParameterTypes;
	protected static MethodHandle getReturnType;
	protected static MethodHandle getDeclaredMethod;

	static {
		try {
			fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
			methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
			getName = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));
			getParameterTypes = lookup.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(Class[].class));
			getReturnType = lookup.findVirtual(methodClass, "getReturnType", MethodType.methodType(Class.class));
			getDeclaredMethod = lookup.findVirtual(Class.class, "getDeclaredMethod", MethodType.methodType(methodClass, String.class, Class[].class));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pass a class, and a methodName as a string to get the returnType and
	 * parameterTypes returned, alonside a MethodType and a ready-to-use 
	 * MethodHandle to use the said method. Useful in places where reflect
	 * package isn't accessible due to the script classLoader.  
	 * @param clazz to search the methodName on
	 * @param methodName in String, no "()"
	 * @return a map with "returnType", "parameterTypes", "methodType" and "methodHandle" keys
	 * @throws Throwable 
	 */
	public static Map<String, Object> findTypesForMethod(Class<?> clazz, String methodName) throws Throwable {
		Object method = getDeclaredMethod.invoke(clazz, methodName);
		Class<?>[] parameterTypes = (Class<?>[]) getParameterTypes.invoke(method);
		Class<?> returnType = (Class<?>) getReturnType.invoke(method);

		MethodType methodType = MethodType.methodType(returnType, parameterTypes);
		MethodHandle methodHandle = MethodHandles.lookup().findVirtual(clazz, methodName, MethodType.methodType(returnType, parameterTypes));
		Map<String, Object> typeMap = new HashMap<String, Object>();
		typeMap.put("returnType", returnType);
		typeMap.put("parameterTypes", parameterTypes);
		typeMap.put("methodType", methodType);
		typeMap.put("methodHandle", methodHandle);
		return typeMap;
	}
}
