package lyravega.utilities;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Arrays;

import lyravega.utilities.logger.lyr_logger;

/**
 * Hosts tools for reflection stuff. These tools are categorized under their
 * own inner classes, and covers a few of the most useful things.
 * <p> As the package uses reflection indirectly through the method handles,
 * it will not trigger game's restriction. Notable methods are listed below,
 * each having their own overloads to accomodate multiple scenarios.
 * @author lyravega
 * @see {@link methodReflection}
 * @see {@link methodReflection#findMethodByName(String, Object, Class...)}
 * @see {@link methodReflection#findMethodByClass(Object, Class, Class...)}
 * @see {@link fieldReflection}
 * @see {@link fieldReflection#findFieldByName(String, Object)}
 * @see {@link fieldReflection#findFieldByClass(Class, Object)}
 */
@SuppressWarnings("unused")
public class lyr_reflectionUtilities {
	protected static final Lookup lookup = MethodHandles.lookup();
	protected static final Class<?> lookupClass = lookup.getClass();

	//#region METHOD STUFF
	/**
	 * Hosts static helper methods to find specific methods in a class/object. These
	 * helpers attempt to instantiate a custom method object and return it to further
	 * inspect the method, but it's mainly used to get a method handle.
	 * <p> Unlike its sibling {@link fieldReflection}, the method is not stored in
	 * the instance, instead the relevant fields that may be useful are cached in it.
	 * Using the instance accessors is thereby safe.
	 * @author lyravega
	 * @see {@link methodReflection#findMethodByName(String, Object, Class...)}
	 * @see {@link methodReflection#findMethodByClass(Object, Class, Class...)}
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
				lyr_logger.fatal("Failed to initialize reflection tools for methods", e);
			}
		}

		/**
		 * Searches a class of an instance or a class for a given {@code methodName},
		 * and returns a custom method object with a few accessors. This returned object
		 * is mostly utilized to get the method handle of the method.
		 * <p> {@code parameterTypes} is optional, however without it the search will
		 * most likely done on every method of the class, till a result is found. With
		 * this optional, specific overloads of the method may be targeted.
		 * <p> {@code declaredOnly} and {@code methodModifier} are overload parameters
		 * which may be utilized to broaden/narrow the search and/or target more specific
		 * methods.
		 * @param methodName as a {@code String}, without any brackets
		 * @param instanceOrClass to search the method on
		 * @param declaredOnly (overload, default {@code true}) to search all or declared methods
		 * @param methodModifier (overload, default {@code null}) to search for a method with specific method modifier
		 * @param parameterTypes (optional) full set of method parameter classes
		 * @return {@link methodReflection}
		 * @see #findMethodByClass(Class, Class, Class...)
		 * @throws Throwable if such a method is cannot be found
		 */
		public static final methodReflection findMethodByName(String methodName, Object instanceOrClass, Class<?>... parameterTypes) throws Throwable {
			return findMethodByName(methodName, instanceOrClass, true, null, parameterTypes);
		}

		/** @see #findMethodByName(String, Class, Class...) */
		public static final methodReflection findMethodByName(String methodName, Object instanceOrClass, boolean declaredOnly, Class<?>... parameterTypes) throws Throwable {
			return findMethodByName(methodName, instanceOrClass, declaredOnly, null, parameterTypes);
		}

		/** @see #findMethodByName(String, Class, Class...) */
		public static final methodReflection findMethodByName(String methodName, Object instanceOrClass, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
			return findMethodByName(methodName, instanceOrClass, true, methodModifier, parameterTypes);
		}

		/** @see #findMethodByName(String, Class, Class...) */
		public static final methodReflection findMethodByName(String methodName, Object instanceOrClass, boolean declaredOnly, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
			Class<?> clazz = instanceOrClass.getClass().equals(Class.class) ? (Class<?>) instanceOrClass : instanceOrClass.getClass();
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

			if (method == null) throw new Throwable("Method with the name '"+methodName+"' was not found in the class '"+clazz.getName()+"'");
			else lyr_logger.reflectionInfo("Method with the name '"+methodName+"' was found in the class '"+clazz.getName()+"'");

			return new methodReflection(method);
		}

		/**
		 * Searches a class of an instance or a class for a method with the passed
		 * {@code returnType} and/or {@code parameterTypes}. Used in cases where
		 * the method name is not available due to obfuscation for example.
		 * <p> {@code parameterTypes} is optional, however without it the search will
		 * most likely done on every method of the class, till a result is found. With
		 * this optional, specific overloads of the method may be targeted.
		 * <p> {@code declaredOnly} and {@code methodModifier} are overload parameters
		 * which may be utilized to broaden/narrow the search and/or target more specific
		 * methods.
		 * @param instanceOrClass to search the method on
		 * @param returnType (one or both) method's return type, can be null
		 * @param declaredOnly (overload, default {@code true}) to search all or declared methods
		 * @param methodModifier (overload, default {@code null}) to search for a method with specific method modifier
		 * @param parameterTypes (one or both) full set of method parameter classes
		 * @return {@link methodReflection}
		 * @see #findMethodByName(String, Class, Class...)
		 * @throws Throwable if such a method is cannot be found
		 */
		public static final methodReflection findMethodByClass(Object instanceOrClass, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
			return findMethodByClass(instanceOrClass, returnType, true, null, parameterTypes);
		}

		/** @see #findMethodByClass(Class, Class, Class...) */
		public static final methodReflection findMethodByClass(Object instanceOrClass, Class<?> returnType, boolean declaredOnly, Class<?>... parameterTypes) throws Throwable {
			return findMethodByClass(instanceOrClass, returnType, declaredOnly, null, parameterTypes);
		}

		/** @see #findMethodByClass(Class, Class, Class...) */
		public static final methodReflection findMethodByClass(Object instanceOrClass, Class<?> returnType, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
			return findMethodByClass(instanceOrClass, returnType, true, methodModifier, parameterTypes);
		}

		/** @see #findMethodByClass(Class, Class, Class...) */
		public static final methodReflection findMethodByClass(Object instanceOrClass, Class<?> returnType, boolean declaredOnly, Integer methodModifier, Class<?>... parameterTypes) throws Throwable {
			Class<?> clazz = instanceOrClass.getClass().equals(Class.class) ? (Class<?>) instanceOrClass : instanceOrClass.getClass();
			Object method = null;
			String methodName = null;

			for (Object currMethod : (declaredOnly) ? clazz.getDeclaredMethods() : clazz.getMethods()) {
				if (returnType != null && !returnType.equals(getReturnType.invoke(currMethod))) continue;
				if (parameterTypes.length > 0 && !Arrays.equals((Class<?>[]) getParameterTypes.invoke(currMethod), parameterTypes)) continue;
				if (methodModifier != null && methodModifier != (int) getModifiers.invoke(currMethod)) continue;

				methodName = (String) getName.invoke(currMethod);
				method = currMethod; break;
			}

			if (method == null) throw new Throwable("Method with the name '"+methodName+"' was not found in the class '"+clazz.getName()+"'");
			else lyr_logger.reflectionInfo("Method with the name '"+methodName+"' was found in the class '"+clazz.getName()+"'");

			return new methodReflection(method);
		}

		/**
		 * A lite method that searches an instance's class or a class for a declared
		 * method name, unreflects its method handle, and attempts to invoke it with the
		 * passed parameters.
		 * <p> Does not handle methods with overloads; will try to invoke the first found
		 * method, regardless of any passed parameters.
		 * <p> Handles instance methods; if {@code instanceOrClass} is an instantiated
		 * object, then it'll be used by the method handle along with the parameters.
		 * @param instanceOrClass to search the method on
		 * @param methodName as a {@code String}, without any brackets
		 * @param parameters to be used as method arguments during invocation
		 * @return anything that the method returns, null for void / no returns
		 * @throws Throwable
		 */
		public static Object invokeDirect(Object instanceOrClass, String methodName, Object... parameters) throws Throwable {
			boolean isClass = instanceOrClass.getClass().equals(Class.class);
			Class<?> clazz = isClass ? (Class<?>) instanceOrClass : instanceOrClass.getClass();

			for (Object method : clazz.getDeclaredMethods()) {
				if (!String.class.cast(getName.invoke(method)).equals(methodName)) continue;

				// lyr_logger.reflectionInfo("Method with the name '"+methodName+"' was found in the class '"+clazz.getName()+"'");
				if (isClass) return MethodHandle.class.cast(unreflect.invoke(lookup, method)).invokeWithArguments(parameters);
				return MethodHandle.class.cast(unreflect.invoke(lookup, method)).bindTo(instanceOrClass).invokeWithArguments(parameters);
			}

			throw new Throwable("Method with the name '"+methodName+"' was not found in '"+clazz.getName()+"'");
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
			this.returnType = this.methodHandle.type().returnType();
			this.parameterTypes = this.methodHandle.type().dropParameterTypes(0, 1).parameterArray();	// MethodType includes the instance class as the first parameter type
			this.methodName = (String) getName.invoke(method);
			this.methodModifiers = (int) getModifiers.invoke(method);
			this.methodType = this.methodHandle.type();
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
			this.parameterTypes = (Class<?>[]) getParameterTypes.invoke(method);	// Method method to get parameter types do not include instance class unlike method handle types do
			this.methodName = (String) getName.invoke(method);
			this.methodModifiers = (int) getModifiers.invoke(method);
			this.methodType = MethodType.methodType(this.returnType, this.parameterTypes);
			this.methodHandle = lookup.findVirtual(clazz, this.methodName, this.methodType);
		}

		public Class<?> getReturnType() { lyr_logger.reflectionInfo("Retrieving 'returnType' of '"+this.methodName+"'"); return this.returnType; }
		public Class<?>[] getParameterTypes() { lyr_logger.reflectionInfo("Retrieving 'parameterTypes' of '"+this.methodName+"'"); return this.parameterTypes; }
		public String getName() { lyr_logger.reflectionInfo("Retrieving 'name' of '"+this.methodName+"'"); return this.methodName; }
		public int getModifiers() { lyr_logger.reflectionInfo("Retrieving 'modifiers' of '"+this.methodName+"'"); return this.methodModifiers; }
		public MethodType getMethodType() { lyr_logger.reflectionInfo("Retrieving 'methodType' of '"+this.methodName+"'"); return this.methodType; }
		public MethodHandle getMethodHandle() { lyr_logger.reflectionInfo("Retrieving 'methodHandle' of '"+this.methodName+"'"); return this.methodHandle; }
	}
	//#endregion
	// END OF METHOD STUFF

	//#region FIELD STUFF
	/**
	 * Hosts static helper methods to find specific fields in a class/object. These
	 * helpers attempt to instantiate an object and return it to further manipulate
	 * the field with a few accessor methods.
	 * <p> Unlike its sibling {@link methodReflection}, the field is stored in
	 * the instance, and most accessors use reflected methods on the stored field.
	 * This is somewhat necessary as field manipulation is more prone to exceptions,
	 * but more than that storing things like the field value would be pointless.
	 * @author lyravega
	 * @see {@link fieldReflection#findFieldByName(String, Object)}
	 * @see {@link fieldReflection#findFieldByClass(Class, Object)}
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
				lyr_logger.fatal("Failed to initialize reflection tools for fields", e);
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

			if (field == null) throw new Throwable("Field with the name '"+fieldName+"' was not found in '"+instanceOrClass.toString()+"'");
			else lyr_logger.reflectionInfo("Field with the name '"+fieldName+"' found in '"+instanceOrClass.toString()+"'");

			return new fieldReflection(field, instanceOrClass);
		}

		/**
		 * A very basic search that searches an instance of an object or a class for a field
		 * with the given field class. Checks if a field's class is equal to or implements
		 * the passed one. Returns after the first found field.
		 * <p> Useless if the search target has multiple fields with the given class, however
		 * might still be useful in a few cases till a better version of this method is
		 * implemented.
		 * @param fieldClass
		 * @param instanceOrClass
		 * @param checkSuper (overload, default true) to check the super class for inherited fields
		 * @param declaredOnly (overload, default true) to check only the declared fields
		 * @return {@link fieldReflection}
		 * @see #findFieldByName(String, Object)
		 * @throws Throwable
		 */
		public static final fieldReflection findFieldByClass(Class<?> fieldClass, Object instanceOrClass) throws Throwable {
			return findFieldByClass(fieldClass, instanceOrClass, true, true);
		}

		/** @see #findFieldByClass(Class, Object) */
		public static final fieldReflection findFieldByClass(Class<?> fieldClass, Object instanceOrClass, boolean checkSuper, boolean declaredOnly) throws Throwable {
			Class<?> clazz = instanceOrClass.getClass().equals(Class.class) ? (Class<?>) instanceOrClass : instanceOrClass.getClass();
			Object field = null;
			String fieldName = null;

			do {
				for (Object currField : (declaredOnly) ? clazz.getDeclaredFields() : clazz.getFields()) {
					Class<?> currFieldClass = (Class<?>) getType.invoke(currField);
					if (!currFieldClass.equals(fieldClass) && !Arrays.asList(currFieldClass.getInterfaces()).contains(fieldClass)) continue;

					fieldName = (String) getName.invoke(currField);
					field = currField; break;
				}
				clazz = checkSuper ? clazz.getSuperclass() : null;
			} while (field == null && clazz != null);

			if (field == null) throw new Throwable("Field with the class '"+fieldClass.getSimpleName()+"' was not found in '"+instanceOrClass.toString()+"'");
			else lyr_logger.reflectionInfo("Field with the class '"+fieldClass.getSimpleName()+"' found in '"+instanceOrClass.toString()+"'");

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
				return get.invoke(this.field, this.instance);
			} catch (Throwable t) {
				lyr_logger.error("Failed to use 'get()' for '"+this.name+"' field", t);
			}; return null;
		}

		public void set(Object value) {
			try {
				set.invoke(this.field, this.instance, value);
			} catch (Throwable t) {
				lyr_logger.error("Failed to use 'set()' for '"+this.name+"' field", t);
			}
		}
	}
	//#endregion
	// END OF FIELD STUFF
}
