package lyravega.tools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Arrays;

/**
 * Reflective operation tools for all such reflection-related classes as 
 * uiTools and ProxyTools.
 * <p>This package is used indirectly and will not trigger the 
 * scriptClassLoader's restriction.
 * <p> The method {@link #findMethodByName} returns information about a
 * method a ready-to-use methodHandle to invoke it.
 * <p> The innerClass {@link methodReflection} stores and provides accessors
 * for this information.
 * @author lyravega
 */
@SuppressWarnings("unused")
public class lyr_reflectionTools implements lyr_logger {
	protected static final Lookup lookup = MethodHandles.lookup();
	protected static final Class<?> lookupClass = lookup.getClass();

	//#region METHOD STUFF
	/**
	 * Provides some accessors related to a method. Reflected information is stored 
	 * locally during construction; accessors don't use any reflection to access the
	 * relevant data unlike {@link fieldReflection}
	 */
	public static final class methodReflection {
		public static Class<?> methodClass;
		private static MethodHandle getName;
		private static MethodHandle getParameterTypes;
		private static MethodHandle getReturnType;
		private static MethodHandle getModifiers;
		private static MethodHandle unreflect;

		static {
			try {	// since these method handles are static and supposed to be used by many, do NOT bind them to an instance
				methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
				getName = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));
				getParameterTypes = lookup.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(Class[].class));
				getReturnType = lookup.findVirtual(methodClass, "getReturnType", MethodType.methodType(Class.class));
				getModifiers = lookup.findVirtual(methodClass, "getModifiers", MethodType.methodType(int.class));
				unreflect = lookup.findVirtual(lookupClass, "unreflect", MethodType.methodType(MethodHandle.class, methodClass));
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
				logger.fatal(logPrefix+"Failed to initialize reflection tools for methods", e);
			}
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
		 * @return {@link methodReflection}
		 * @see #findMethodByClass(Class, Class, Class...)
		 * @throws Throwable if such a method is cannot be found
		 */
		public static final methodReflection findMethodByName(String methodName, Class<?> clazz, Class<?>... parameterTypes) throws Throwable {
			return findMethodByName(methodName, clazz, true, null, parameterTypes);
		}

		/** @see #findMethodByName(String, Class, Class...) */
		public static final methodReflection findMethodByName(String methodName, Class<?> clazz, boolean declaredOnly, Class<?>... parameterTypes) throws Throwable {
			return findMethodByName(methodName, clazz, declaredOnly, null, parameterTypes);
		}

		/** @see #findMethodByName(String, Class, Class...) */
		public static final methodReflection findMethodByName(String methodName, Class<?> clazz, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
			return findMethodByName(methodName, clazz, true, methodModifier, parameterTypes);
		}

		/** @see #findMethodByName(String, Class, Class...) */
		public static final methodReflection findMethodByName(String methodName, Class<?> clazz, boolean declaredOnly, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
			Object method = null;

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
			else logger.info(logPrefix+"Method with the name '"+methodName+"' found in the class '"+clazz.getName()+"'");

			return new methodReflection(method);
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
		 * @return {@link methodReflection}
		 * @see #findMethodByName(String, Class, Class...)
		 * @throws Throwable if such a method is cannot be found
		 */
		public static final methodReflection findMethodByClass(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
			return findMethodByClass(clazz, returnType, true, null, parameterTypes);
		}

		/** @see #findMethodByClass(Class, Class, Class...) */
		public static final methodReflection findMethodByClass(Class<?> clazz, Class<?> returnType, boolean declaredOnly, Class<?>... parameterTypes) throws Throwable {
			return findMethodByClass(clazz, returnType, declaredOnly, null, parameterTypes);
		}

		/** @see #findMethodByClass(Class, Class, Class...) */
		public static final methodReflection findMethodByClass(Class<?> clazz, Class<?> returnType, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
			return findMethodByClass(clazz, returnType, true, methodModifier, parameterTypes);
		}

		/** @see #findMethodByClass(Class, Class, Class...) */
		public static final methodReflection findMethodByClass(Class<?> clazz, Class<?> returnType, boolean declaredOnly, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
			Object method = null;
			String methodName = null;

			for (Object currMethod : (declaredOnly) ? clazz.getDeclaredMethods() : clazz.getMethods()) {
				if (returnType != null && !returnType.equals(getReturnType.invoke(currMethod))) continue;
				if (parameterTypes.length > 0 && !Arrays.equals((Class<?>[]) getParameterTypes.invoke(currMethod), parameterTypes)) continue;
				if (methodModifier != null && methodModifier != (int) getModifiers.invoke(currMethod)) continue;

				methodName = (String) getName.invoke(currMethod);
				method = currMethod; break;
			}

			if (method == null) throw new Throwable(logPrefix+"Method"+(returnType != null ? " with the return type '"+returnType.toString()+"'" : "")+(parameterTypes.length > 0 ? " parameter types '"+parameterTypes.toString()+"'" : "")+" was not found in the class '"+clazz.getName()+"'");
			else logger.info(logPrefix+"Method with the name '"+methodName+"' found in the class '"+clazz.getName()+"'");

			return new methodReflection(method);
		}

		// private final Object method;	// not necessary since accessors do not use this, so it is tossed away
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
		private methodReflection(Object method) throws Throwable {
			// this.method = method;
			this.methodHandle = (MethodHandle) unreflect.invoke(lookup, method);
			this.returnType = methodHandle.type().returnType();
			this.parameterTypes = methodHandle.type().dropParameterTypes(0, 1).parameterArray();	// MethodHandle query for parameter types contains return type, drop it
			this.methodName = (String) getName.invoke(method);
			this.methodModifiers = (int) getModifiers.invoke(method);
			this.methodType = methodHandle.type();
		}

		/**
		 * Constructs a method info object with a few getters to access information
		 * about the method. Mostly used to retrieve methodHandles
		 * <p> Deprecated as it is the older method that directly queries the method
		 * object with method methods to fill in the relevant fields. Class is required
		 * for the method handle lookup
		 * @param method as an object
		 * @param clazz of the method's location
		 * @throws Throwable
		 */
		@Deprecated
		private methodReflection(Object method, Class<?> clazz) throws Throwable {
			// this.method = method;
			this.returnType = (Class<?>) getReturnType.invoke(method);
			this.parameterTypes = (Class<?>[]) getParameterTypes.invoke(method);	// Method reflection for parameter types does not contain return type
			this.methodName = (String) getName.invoke(method);
			this.methodModifiers = (int) getModifiers.invoke(method);
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
	//#endregion
	// END OF METHOD STUFF

	//#region FIELD STUFF
	/**
	 * Provides some accessors related to a field. Accessors use reflection to get
	 * the job done, unlike {@link methodReflection}. This is somewhat necessary as
	 * field manipulation is more prone to exceptions, and storing / changing the
	 * field's value in an instance of this object would be pointless anyway
	 */
	public static final class fieldReflection {
		public static Class<?> fieldClass;
		private static MethodHandle isAccessible;
		private static MethodHandle setAccessible;
		private static MethodHandle getName;
		private static MethodHandle getModifiers;
		private static MethodHandle getType;
		private static MethodHandle get;
		private static MethodHandle set;

		static {
			try {	// since these method handles are static and supposed to be used by many, do NOT bind them to an instance
				fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
				isAccessible = lookup.findVirtual(fieldClass, "isAccessible", MethodType.methodType(boolean.class));
				setAccessible = lookup.findVirtual(fieldClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
				getName = lookup.findVirtual(fieldClass, "getName", MethodType.methodType(String.class));
				getModifiers = lookup.findVirtual(fieldClass, "getModifiers", MethodType.methodType(int.class));
				getType = lookup.findVirtual(fieldClass, "getType", MethodType.methodType(Class.class));
				get = lookup.findVirtual(fieldClass, "get", MethodType.methodType(Object.class, Object.class));
				set = lookup.findVirtual(fieldClass, "set", MethodType.methodType(void.class, Object.class, Object.class));
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
				logger.fatal(logPrefix+"Failed to initialize reflection tools for fields", e);
			}
		}

		/**
		 * Searches a class or an instance of an object for a given field name, and returns
		 * a field info object with ready-to-use methods on the field. Attempts to set field
		 * as accessible while returning the constructed object.
		 * <p> The passed object can either be an instance or a class. If a class is passed,
		 * then only static fields will be searched for, since there are no instance fields
		 * to check for in that case. Superclasses may be checked with another argument.
		 * @param fieldName
		 * @param instanceOrClass can either be an instance of an object, or its class
		 * @param checkSuper (overload, default true) to check the super class for inherited fields
		 * @param declaredOnly (overload, default true) to check only the declared fields
		 * @return {@link fieldReflection}
		 * @see #findFieldByClass(Class, Object)
		 * @throws Throwable if such a field is not found or errors happen during construction
		 */
		public static final fieldReflection findFieldByName(String fieldName, Object instanceOrClass) throws Throwable {
			return findFieldByName(fieldName, instanceOrClass, true, true);		
		}

		/** @see #findFieldByName(String, Object) */
		public static final fieldReflection findFieldByName(String fieldName, Object instanceOrClass, boolean checkSuper, boolean declaredOnly) throws Throwable {
			Class<?> clazz = instanceOrClass.getClass().equals(Class.class) ? (Class<?>) instanceOrClass : instanceOrClass.getClass();
			Object field = null;

			do {
				try {
					field = (declaredOnly) ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
				} catch (Exception e) {
					clazz = checkSuper ? clazz.getSuperclass() : null;
				}
			} while (field == null && clazz != null);

			if (field == null) throw new Throwable(logPrefix+"Field with the name '"+fieldName+"' was not found in '"+instanceOrClass.toString()+"'");
			else logger.info("Field with the name '"+fieldName+"' found in '"+instanceOrClass.toString()+"'");

			return new fieldReflection(field, instanceOrClass);
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
		 * @return {@link fieldReflection}
		 * @see #findFieldByName(String, Object)
		 * @throws Throwable
		 */
		public static final fieldReflection findFieldByClass(Class<?> fieldClazz, Object instanceOrClass) throws Throwable {
			return findFieldByClass(fieldClazz, instanceOrClass, true, true);		
		}

		/** @see #findFieldByClass(Class, Object) */
		public static final fieldReflection findFieldByClass(Class<?> fieldClazz, Object instanceOrClass, boolean checkSuper, boolean declaredOnly) throws Throwable {
			Class<?> clazz = instanceOrClass.getClass().equals(Class.class) ? (Class<?>) instanceOrClass : instanceOrClass.getClass();
			Object field = null;
			String fieldName = null;

			do {
				for (Object currField : (declaredOnly) ? clazz.getDeclaredFields() : clazz.getFields()) {
					if (!getType.invoke(field).equals(fieldClazz)) continue;

					fieldName = (String) getName.invoke(currField);
					field = currField; break;
				}
				clazz = checkSuper ? clazz.getSuperclass() : null;
			} while (field == null && clazz != null);

			if (field == null) throw new Throwable(logPrefix+"Field with the name '"+fieldName+"' was not found in '"+instanceOrClass.toString()+"'");
			else logger.info("Field with the name '"+fieldName+"' found in '"+instanceOrClass.toString()+"'");

			return new fieldReflection(field, instanceOrClass);
		}

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
		private fieldReflection(Object field, Object instance) throws Throwable {
			setAccessible.invoke(field, true);

			this.name = (String) getName.invoke(field);
			this.field = field;
			this.instance = instance;
		}

		public String getName() { return this.name; }

		public Object get() {
			try {
				return get.invoke(field, instance);
			} catch (Throwable t) {
				logger.error(logPrefix+"Failed to use 'get()' for '"+this.name+"' field", t);
			}; return null;
		}

		public void set(Object value) {
			try {
				set.invoke(field, instance, value);
			} catch (Throwable t) {
				logger.error(logPrefix+"Failed to use 'set()' for '"+this.name+"' field", t);
			}
		}
	}
	//#endregion
	// END OF FIELD STUFF
}
