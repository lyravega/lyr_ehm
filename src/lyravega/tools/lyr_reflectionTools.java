package lyravega.tools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.*;
import java.lang.invoke.MethodType;

import com.fs.starfarer.campaign.ui.marketinfo.f;


/**
 * Reflective operation tools for all such reflection-related classes as 
 * uiTools and ProxyTools.
 * <p>This package is used indirectly and will not trigger the 
 * scriptClassLoader's restriction.
 * <p> The method {@link #findMethodByName} returns information about a
 * method a ready-to-use methodHandle to invoke it.
 * <p> The innerClass {@link methodInfo} stores and provides accessors
 * for this information.
 * @author lyravega
 */
@SuppressWarnings("unused")
public class lyr_reflectionTools implements lyr_logger {
	protected static final Lookup lookup = MethodHandles.lookup();
	protected static final Class<?> lookupClass = lookup.getClass();

	private static class methodStuff {
		private static Class<?> methodClass;
		private static MethodHandle getName;
		private static MethodHandle getParameterTypes;
		private static MethodHandle getReturnType;
		private static MethodHandle getModifiers;
		private static MethodHandle unreflect;

		static {
			try {
				methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
				methodStuff.getName = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));
				methodStuff.getParameterTypes = lookup.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(Class[].class));
				methodStuff.getReturnType = lookup.findVirtual(methodClass, "getReturnType", MethodType.methodType(Class.class));
				methodStuff.getModifiers = lookup.findVirtual(methodClass, "getModifiers", MethodType.methodType(int.class));
				methodStuff.unreflect = lookup.findVirtual(lookupClass, "unreflect", MethodType.methodType(MethodHandle.class, methodClass));
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
				logger.fatal(logPrefix+"Failed to initialize reflection tools for methods", e);
			}
		}
	}

	private static class fieldStuff {
		private static Class<?> fieldClass;
		private static MethodHandle isAccessible;
		private static MethodHandle setAccessible;
		private static MethodHandle getName;
		private static MethodHandle getModifiers;
		private static MethodHandle getType;
		private static MethodHandle get;
		private static MethodHandle set;

		static {
			try {
				fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
				fieldStuff.isAccessible = lookup.findVirtual(fieldClass, "isAccessible", MethodType.methodType(boolean.class));
				fieldStuff.setAccessible = lookup.findVirtual(fieldClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
				fieldStuff.getName = lookup.findVirtual(fieldClass, "getName", MethodType.methodType(String.class));
				fieldStuff.getModifiers = lookup.findVirtual(fieldClass, "getModifiers", MethodType.methodType(int.class));
				fieldStuff.getType = lookup.findVirtual(fieldClass, "getType", MethodType.methodType(Class.class));
				fieldStuff.get = lookup.findVirtual(fieldClass, "get", MethodType.methodType(Object.class, Object.class));
				fieldStuff.set = lookup.findVirtual(fieldClass, "set", MethodType.methodType(void.class, Object.class, Object.class));
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
				logger.fatal(logPrefix+"Failed to initialize reflection tools for fields", e);
			}
		}
	}

	/**
	 * Provides some accessors related to a method. Reflected information is stored 
	 * locally during construction; accessors don't use any reflection to access the
	 * relevant data unlike {@link fieldInfo}
	 */
	public static final class methodInfo {
		private final Object method;
		private final MethodHandle methodHandle;
		private final Class<?> returnType;
		private final Class<?>[] parameterTypes;
		private final String methodName;
		private final int methodModifiers;
		private final MethodType methodType;

		/**
		 * Constructs a method info object with a few getters to access information
		 * about the method. Mostly used to retrieve methodHandles
		 * <p> Uses unreflect to expose method's handle, which is then used to store
		 * the relevant information about the method
		 * @param method as an object
		 * @throws Throwable
		 */
		private methodInfo(Object method) throws Throwable {
			this.method = method;
			this.methodHandle = (MethodHandle) methodStuff.unreflect.invoke(lookup, method);
			this.returnType = methodHandle.type().returnType();
			this.parameterTypes = methodHandle.type().dropParameterTypes(0, 1).parameterArray();
			this.methodName = (String) methodStuff.getName.invoke(method);
			this.methodModifiers = (int) methodStuff.getModifiers.invoke(method);
			this.methodType = methodHandle.type();
		}

		/**
		 * Constructs a method info object with a few getters to access information
		 * about the method. Mostly used to retrieve methodHandles
		 * <p> Deprecated as it is the older method that directly queries the method
		 * object with method methods to fill in the relevant fields
		 * @param method as an object
		 * @param clazz of the method's location
		 * @throws Throwable
		 */
		@Deprecated
		private methodInfo(Object method, Class<?> clazz) throws Throwable {
			this.method = method;
			this.returnType = (Class<?>) methodStuff.getReturnType.invoke(method);
			this.parameterTypes = (Class<?>[]) methodStuff.getParameterTypes.invoke(method);
			this.methodName = (String) methodStuff.getName.invoke(method);
			this.methodModifiers = (int) methodStuff.getModifiers.invoke(method);
			this.methodType = MethodType.methodType(returnType, parameterTypes);
			this.methodHandle = lookup.findVirtual(clazz, methodName, methodType);
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
	 * <p> {@code parameterTypes} can be ignored, however if given alongside a 
	 * {@code methodName}, they'll be used to perform a more specific search that
	 * can also target overloaded methods.
	 * @param methodName as String, no "()"
	 * @param clazz to search the methodName on
	 * @param declaredOnly (overload, default {@code true}) to search declared only or all methods
	 * @param methodModifier (overload, default {@code null}) to search a method with a specific modifier
	 * @param parameterTypes (optional) full set of parameters, if available and needed
	 * @return {@link methodInfo}
	 * @see #findMethodByClass(Class, Class, Class...)
	 * @throws Throwable if such a method is cannot be found
	 */
	public static final methodInfo findMethodByName(String methodName, Class<?> clazz, Class<?>... parameterTypes) throws Throwable {
		return findMethodByName(methodName, clazz, true, null, parameterTypes);
	}

	/** @see #findMethodByName(String, Class, Class...) */
	public static final methodInfo findMethodByName(String methodName, Class<?> clazz, boolean declaredOnly, Class<?>... parameterTypes) throws Throwable {
		return findMethodByName(methodName, clazz, declaredOnly, null, parameterTypes);
	}

	/** @see #findMethodByName(String, Class, Class...) */
	public static final methodInfo findMethodByName(String methodName, Class<?> clazz, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
		return findMethodByName(methodName, clazz, true, methodModifier, parameterTypes);
	}

	/** @see #findMethodByName(String, Class, Class...) */
	public static final methodInfo findMethodByName(String methodName, Class<?> clazz, boolean declaredOnly, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
		Object method = null;

		try {
			method = (declaredOnly) ? clazz.getDeclaredMethod(methodName, parameterTypes) : clazz.getMethod(methodName, parameterTypes);
		} catch (Throwable t) { // searches all the methods with the passed name if the above fails, and uses the FIRST found one
			for (Object currMethod : (declaredOnly) ? clazz.getDeclaredMethods() : clazz.getMethods()) {
				if (!String.class.cast(methodStuff.getName.invoke(currMethod)).equals(methodName)) continue;
				if (methodModifier != null && methodModifier != (int) methodStuff.getModifiers.invoke(currMethod)) continue;
	
				method = currMethod; break;
			}
		}

		if (method == null) throw new Throwable(logPrefix+"Method with the name '"+methodName+"' was not found in the class '"+clazz.getName()+"'");
		else logger.info(logPrefix+"Method with the name '"+methodName+"' found in the class '"+clazz.getName()+"'");

		return new methodInfo(method);
	}

	/**
	 * Search for a method in a class through the passed {@code returnType} and/or
	 * {@code parameterTypes}. Used in cases where the method name is not available
	 * (due to obfuscation for example).
	 * <p> Like its sibling, returns a methodInfo. The only difference is whether
	 * the search is done through its name, or classes of its parameters.
	 * @param clazz to search the method in
	 * @param returnType of the method being searched for, can be null
	 * @param declaredOnly (overload, default {@code true}) to search declared only or all methods
	 * @param methodModifier (overload, default {@code null}) to search a method with a specific modifier
	 * @param parameterTypes (optional) full set of parameters, if available and needed
	 * @return {@link methodInfo}
	 * @see #findMethodByName(String, Class, Class...)
	 * @throws Throwable if such a method is cannot be found
	 */
	public static final methodInfo findMethodByClass(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
		return findMethodByClass(clazz, returnType, true, null, parameterTypes);
	}

	/** @see #findMethodByClass(Class, Class, Class...) */
	public static final methodInfo findMethodByClass(Class<?> clazz, Class<?> returnType, boolean declaredOnly, Class<?>... parameterTypes) throws Throwable {
		return findMethodByClass(clazz, returnType, declaredOnly, null, parameterTypes);
	}

	/** @see #findMethodByClass(Class, Class, Class...) */
	public static final methodInfo findMethodByClass(Class<?> clazz, Class<?> returnType, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
		return findMethodByClass(clazz, returnType, true, methodModifier, parameterTypes);
	}

	/** @see #findMethodByClass(Class, Class, Class...) */
	public static final methodInfo findMethodByClass(Class<?> clazz, Class<?> returnType, boolean declaredOnly, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
		Object method = null;
		String methodName = null;

		for (Object currMethod : (declaredOnly) ? clazz.getDeclaredMethods() : clazz.getMethods()) {
			if (returnType != null && !returnType.equals(methodStuff.getReturnType.invoke(currMethod))) continue;
			if (parameterTypes.length > 0 && !Arrays.equals((Class<?>[]) methodStuff.getParameterTypes.invoke(currMethod), parameterTypes)) continue;
			if (methodModifier != null && methodModifier != (int) methodStuff.getModifiers.invoke(currMethod)) continue;

			methodName = (String) methodStuff.getName.invoke(currMethod);
			method = currMethod; break;
		}

		if (method == null) throw new Throwable(logPrefix+"Method"+(returnType != null ? " with the return type '"+returnType.toString()+"'" : "")+(parameterTypes.length > 0 ? " parameter types '"+parameterTypes.toString()+"'" : "")+" was not found in the class '"+clazz.getName()+"'");
		else logger.info(logPrefix+"Method with the name '"+methodName+"' found in the class '"+clazz.getName()+"'");

		return new methodInfo(method);
	}

	/**
	 * Provides some accessors related to a field. Accessors use reflection to get
	 * the job done, unlike {@link methodInfo}. This is somewhat necessary as
	 * field manipulation is more prone to exceptions, and storing / changing the
	 * field's value in an instance of this object would be pointless anyway
	 */
	public static final class fieldInfo {
		private final String name;
		private final Object field;
		private final Object instance;

		/**
		 * Constructs a field info object with a few methods to access and manipulate
		 * the data stored in it. Most, if not all of the accessors use reflection when
		 * they are called, because it is then reflection is needed.
		 * <p> During construction, will attempt to make the field accessible if it is
		 * not. If the operation fails, will throw an error; rest of the methods would
		 * be useless in that case anyway.
		 * @param field as an object
		 * @param instance as an object, 
		 * @throws Throwable
		 */
		private fieldInfo(Object field, Object instance) throws Throwable {
			if ((boolean) fieldStuff.isAccessible.invoke(field)) fieldStuff.setAccessible.invoke(field, true);

			this.name = (String) fieldStuff.getName.invoke(field);
			this.field = field;
			this.instance = instance;
		}

		public Object get() {
			try {
				return fieldStuff.get.invoke(field, instance);
			} catch (Throwable t) {
				logger.error(logPrefix+"Failed to use 'get()' for '"+this.name+"' field", t);
			}; return null;
		}

		public void set(Object value) {
			try {
				fieldStuff.set.invoke(field, instance, value);
			} catch (Throwable t) {
				logger.error(logPrefix+"Failed to use 'set()' for '"+this.name+"' field", t);
			}
		}
	}

	/**
	 * Searches a class or an instance of an object for a given field name, and returns
	 * a field info object with ready-to-use methods on the field.
	 * <p> The passed object can either be an instance or a class. If a class is passed,
	 * then only static fields will be searched for, since there are no instance fields
	 * to check for in that case. Superclasses may be checked with another argument.
	 * @param fieldName
	 * @param instanceOrClass can either be an instance of an object, or its class
	 * @param checkSuper (overload, default true) to check the super class for inherited fields
	 * @param declaredOnly (overload, default true) to check only the declared fields
	 * @return {@link fieldInfo}
	 * @see #findFieldByClass(Class, Object)
	 * @throws Throwable if such a field is not found or errors happen during construction
	 */
	public static final fieldInfo findFieldByName(String fieldName, Object instanceOrClass) throws Throwable {
		return findFieldByName(fieldName, instanceOrClass, true, true);		
	}

	/** @see #findFieldByName(String, Object) */
	public static final fieldInfo findFieldByName(String fieldName, Object instanceOrClass, boolean checkSuper, boolean declaredOnly) throws Throwable {
		Class<?> clazz = instanceOrClass.getClass().equals(Class.class) ? (Class<?>) instanceOrClass : instanceOrClass.getClass();
		Object field = null;

		do {
			try {
				field = (declaredOnly) ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
			} catch (Exception e) {
				clazz = checkSuper ? clazz.getSuperclass() : null;
			}
		} while (field == null && clazz != null);

		if (field == null) throw new Throwable(logPrefix+"Field with the name '"+fieldName+"' was not found in the class '"+clazz.getName()+"'");
		else logger.info("Field with the name '"+fieldName+"' found in the class '"+clazz.getName()+"'");

		return new fieldInfo(field, instanceOrClass);
	}

	/**
	 * A very basic search that searches an instance of an object or a class for a field
	 * with the given field class. Returns after the first found field.
	 * <p> Useless if the search target has multiple fields with the given class, however
	 * might still be useful in a few cases till a better version of this method is
	 * implemented.
	 * @param fieldClazz
	 * @param instanceOrClass
	 * @param checkSuper (overload, default true) to check the super class for inherited fields
	 * @param declaredOnly (overload, default true) to check only the declared fields
	 * @return {@link fieldInfo}
	 * @see #findFieldByName(String, Object)
	 * @throws Throwable
	 */
	public static final fieldInfo findFieldByClass(Class<?> fieldClazz, Object instanceOrClass) throws Throwable {
		return findFieldByClass(fieldClazz, instanceOrClass, true, true);		
	}

	/** @see #findFieldByClass(Class, Object) */
	public static final fieldInfo findFieldByClass(Class<?> fieldClazz, Object instanceOrClass, boolean checkSuper, boolean declaredOnly) throws Throwable {
		Class<?> clazz = instanceOrClass.getClass().equals(Class.class) ? (Class<?>) instanceOrClass : instanceOrClass.getClass();
		Object field = null;
		String fieldName = null;

		do {
			for (Object currField : (declaredOnly) ? clazz.getDeclaredFields() : clazz.getFields()) {
				if (!fieldStuff.getType.invoke(field).equals(fieldClazz)) continue;

				fieldName = (String) fieldStuff.getName.invoke(currField);
				field = currField; break;
			}
			clazz = checkSuper ? clazz.getSuperclass() : null;
		} while (field == null && clazz != null);

		if (field == null) throw new Throwable(logPrefix+"Field with the name '"+fieldName+"' was not found in the class '"+clazz.getName()+"'");
		else logger.info("Field with the name '"+fieldName+"' found in the class '"+clazz.getName()+"'");

		return new fieldInfo(field, instanceOrClass);
	}
}
