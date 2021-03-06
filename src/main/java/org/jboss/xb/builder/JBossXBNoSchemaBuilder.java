/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.xb.builder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.jboss.beans.info.spi.BeanAccessMode;
import org.jboss.beans.info.spi.BeanInfo;
import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.joinpoint.plugins.Config;
import org.jboss.logging.Logger;
import org.jboss.reflect.plugins.introspection.ParameterizedClassInfo;
import org.jboss.reflect.spi.ArrayInfo;
import org.jboss.reflect.spi.ClassInfo;
import org.jboss.reflect.spi.EnumInfo;
import org.jboss.reflect.spi.FieldInfo;
import org.jboss.reflect.spi.MethodInfo;
import org.jboss.reflect.spi.PackageInfo;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.reflect.spi.TypeInfoFactory;
import org.jboss.xb.annotations.JBossXmlAdaptedType;
import org.jboss.xb.annotations.JBossXmlAdaptedTypes;
import org.jboss.xb.annotations.JBossXmlAttribute;
import org.jboss.xb.annotations.JBossXmlChild;
import org.jboss.xb.annotations.JBossXmlChildWildcard;
import org.jboss.xb.annotations.JBossXmlChildren;
import org.jboss.xb.annotations.JBossXmlCollection;
import org.jboss.xb.annotations.JBossXmlConstants;
import org.jboss.xb.annotations.JBossXmlGroup;
import org.jboss.xb.annotations.JBossXmlGroupText;
import org.jboss.xb.annotations.JBossXmlGroupWildcard;
import org.jboss.xb.annotations.JBossXmlMapEntry;
import org.jboss.xb.annotations.JBossXmlMapKeyAttribute;
import org.jboss.xb.annotations.JBossXmlMapKeyElement;
import org.jboss.xb.annotations.JBossXmlMapValueAttribute;
import org.jboss.xb.annotations.JBossXmlMapValueElement;
import org.jboss.xb.annotations.JBossXmlModelGroup;
import org.jboss.xb.annotations.JBossXmlNoElements;
import org.jboss.xb.annotations.JBossXmlNsPrefix;
import org.jboss.xb.annotations.JBossXmlPreserveWhitespace;
import org.jboss.xb.annotations.JBossXmlAccessMode;
import org.jboss.xb.annotations.JBossXmlSchema;
import org.jboss.xb.annotations.JBossXmlTransient;
import org.jboss.xb.annotations.JBossXmlTransients;
import org.jboss.xb.annotations.JBossXmlType;
import org.jboss.xb.annotations.JBossXmlValue;
import org.jboss.xb.binding.JBossXBRuntimeException;
import org.jboss.xb.binding.SimpleTypeBindings;
import org.jboss.xb.binding.Util;
import org.jboss.xb.binding.sunday.unmarshalling.CollectionRepeatableParticleHandler;
import org.jboss.xb.binding.sunday.unmarshalling.AllBinding;
import org.jboss.xb.binding.sunday.unmarshalling.AnyAttributeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.AttributeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.AttributeHandler;
import org.jboss.xb.binding.sunday.unmarshalling.CharactersHandler;
import org.jboss.xb.binding.sunday.unmarshalling.ChoiceBinding;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultElementInterceptor;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultHandlers;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ModelGroupBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ParticleBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ParticleHandler;
import org.jboss.xb.binding.sunday.unmarshalling.RepeatableParticleHandler;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SequenceBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SimpleTypeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.TermBinding;
import org.jboss.xb.binding.sunday.unmarshalling.TypeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.UnorderedSequenceBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ValueAdapter;
import org.jboss.xb.binding.sunday.unmarshalling.WildcardBinding;
import org.jboss.xb.builder.runtime.AbstractPropertyHandler;
import org.jboss.xb.builder.runtime.AnyAttributePropertyHandler;
import org.jboss.xb.builder.runtime.AppendingArrayRepeatableHandler;
import org.jboss.xb.builder.runtime.ArrayWrapperRepeatableParticleHandler;
import org.jboss.xb.builder.runtime.BeanHandler;
import org.jboss.xb.builder.runtime.BuilderParticleHandler;
import org.jboss.xb.builder.runtime.BuilderSimpleParticleHandler;
import org.jboss.xb.builder.runtime.ChildCollectionInterceptor;
import org.jboss.xb.builder.runtime.ChildCollectionWildcardHandler;
import org.jboss.xb.builder.runtime.ChildWildcardHandler;
import org.jboss.xb.builder.runtime.CollectionPropertyHandler;
import org.jboss.xb.builder.runtime.CollectionPropertyWildcardHandler;
import org.jboss.xb.builder.runtime.DOMHandler;
import org.jboss.xb.builder.runtime.DefaultMapEntry;
import org.jboss.xb.builder.runtime.EnumValueAdapter;
import org.jboss.xb.builder.runtime.GroupBeanHandler;
import org.jboss.xb.builder.runtime.MapPropertyHandler;
import org.jboss.xb.builder.runtime.NonXmlAnyElementDOMElementPropertyHandler;
import org.jboss.xb.builder.runtime.PropertyHandler;
import org.jboss.xb.builder.runtime.PropertyInterceptor;
import org.jboss.xb.builder.runtime.PropertyWildcardHandler;
import org.jboss.xb.builder.runtime.SetParentOverrideHandler;
import org.jboss.xb.builder.runtime.ValueHandler;
import org.jboss.xb.builder.runtime.WrapperBeanAdapterFactory;
import org.jboss.xb.spi.BeanAdapterBuilder;
import org.jboss.xb.spi.BeanAdapterFactory;
import org.jboss.xb.spi.DefaultBeanAdapterBuilder;
import org.w3c.dom.Element;

/**
 * JBossXBNoSchemaBuilder.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class JBossXBNoSchemaBuilder
{
   /** The log */
   private static final Logger log = Logger.getLogger(JBossXBBuilder.class);

   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();

   /** The schema binding */
   private SchemaBinding schemaBinding;

   /** The root type */
   private ClassInfo root;

   /** The namespace */
   private String defaultNamespace;

   /** The attribute form */
   private XmlNsForm attributeForm = XmlNsForm.UNSET;

   /** The element form */
   private XmlNsForm elementForm = XmlNsForm.UNSET;

   /** A cache of types */
   private Map<TypeInfo, TypeBinding> typeCache = new HashMap<TypeInfo, TypeBinding>();

   /** A root elements we have processed */
   private Map<TypeInfo, ElementBinding> rootCache = new HashMap<TypeInfo, ElementBinding>();

   /** The current location */
   private Stack<Location> locations = new Stack<Location>();

   private ModelGroupFactory groupFactory = DefaultModelGroupFactory.INSTANCE;
   
   private boolean useUnorderedSequence;
   private boolean sequencesRequirePropOrder;
   private boolean elementSetParentHandler;
   private boolean repeatableParticleHandlers;
   // this is repeatableParticleHandlers in a specific context while the one above is global default
   private boolean currentRepeatableHandlers;

   private BeanAccessMode beanAccessMode = BeanAccessMode.STANDARD;
   
   /** transient property names by type name */
   private Map<String, Set<String>> jbossXmlTransients = Collections.emptyMap();

   /**
    * Create a new JBossXBNoSchemaBuilder.
    * 
    * @param root the root class info
    * @throws IllegalArgumentException for a null root class info
    */
   public JBossXBNoSchemaBuilder(ClassInfo root)
   {
      if (root == null)
         throw new IllegalArgumentException("Null root");

      this.root = root;
   }

   public boolean isUseUnorderedSequence()
   {
      return useUnorderedSequence;
   }
   
   public void setUseUnorderedSequence(boolean useUnorderedSequence)
   {
      this.useUnorderedSequence = useUnorderedSequence;
      if(useUnorderedSequence)
         groupFactory = UnorderedSequenceModelGroupFactory.INSTANCE;
      else
         groupFactory = DefaultModelGroupFactory.INSTANCE;
   }
   
   public boolean isSequenceRequirePropOrder()
   {
      return sequencesRequirePropOrder;
   }
   
   public void setSequencesRequirePropOrder(boolean sequencesRequirePropOrder)
   {
      this.sequencesRequirePropOrder = sequencesRequirePropOrder;
   }

   public boolean isElementSetParentOverrideHandler()
   {
      return elementSetParentHandler;
   }

   public void setElementSetParentOverrideHandler(boolean elementSetParentHandler)
   {
      this.elementSetParentHandler = elementSetParentHandler;
   }

   public boolean isRepeatableParticleHandlers()
   {
      return repeatableParticleHandlers;
   }
   
   public void setRepeatableParticleHandlers(boolean repeatableParticleHandlers)
   {
      this.repeatableParticleHandlers = repeatableParticleHandlers;
   }
   
   /**
    * Build the schema
    * 
    * @return the schema
    */
   public SchemaBinding build()
   {
      // Initialize the schema
      schemaBinding = new SchemaBinding();
      JBossXBBuilder.initSchema(schemaBinding, root);
      initBuilder();
      createRootElements();
      return schemaBinding;
   }

   /**
    * Builds schema binding components from the class and adds them to the SchemaBinding
    * passed in the argument.
    * Note, schema initialization step (processing of schema-related class- and package-level annotations) will be skipped.
    * 
    * @param schema  SchemaBinding to add the built binding components to
    */
   public void build(SchemaBinding schema)
   {
      if(schema == null)
         throw new IllegalArgumentException("Null schema");
      schemaBinding = schema;
      initBuilder();
      createRootElements();
   }

   /**
    * Initialise the builder
    */
   protected void initBuilder()
   {
      if (trace)
         log.trace("Building schema for " + root.getName() + " schemaBinding=" + schemaBinding);

      // Remember the default namespace
      if (defaultNamespace == null)
      {
         defaultNamespace = (String) schemaBinding.getNamespaces().iterator().next();
      }

      JBossXmlSchema jbossXmlSchema = root.getUnderlyingAnnotation(JBossXmlSchema.class);
      if (jbossXmlSchema != null)
      {
         attributeForm = jbossXmlSchema.attributeFormDefault();
         elementForm = jbossXmlSchema.elementFormDefault();
      }

      // Look for an annotation
      PackageInfo packageInfo = root.getPackage();
      if (packageInfo != null)
      {
         jbossXmlSchema = root.getUnderlyingAnnotation(JBossXmlSchema.class);
         if (jbossXmlSchema != null)
         {
            if (attributeForm == XmlNsForm.UNSET)
               attributeForm = jbossXmlSchema.attributeFormDefault();
            if (elementForm == XmlNsForm.UNSET)
               elementForm = jbossXmlSchema.elementFormDefault();
         }

         XmlSchema xmlSchema = packageInfo.getUnderlyingAnnotation(XmlSchema.class);
         if (xmlSchema != null)
         {
            String namespace = xmlSchema.namespace();
            if (JBossXmlConstants.DEFAULT.equals(xmlSchema) == false && XMLConstants.NULL_NS_URI.equals(defaultNamespace))
            {
               defaultNamespace = namespace;
               addNamespace(defaultNamespace, true);
            }

            if (attributeForm == XmlNsForm.UNSET)
               attributeForm = xmlSchema.attributeFormDefault();
            if (elementForm == XmlNsForm.UNSET)
               elementForm = xmlSchema.elementFormDefault();
         }

         // Check for adapted types
         JBossXmlAdaptedTypes adaptedTypes = packageInfo.getUnderlyingAnnotation(JBossXmlAdaptedTypes.class);
         if (adaptedTypes != null)
         {
            for (JBossXmlAdaptedType adaptedType : adaptedTypes.value())
               generateAdaptedType(adaptedType);
         }
         JBossXmlAdaptedType adaptedType = packageInfo.getUnderlyingAnnotation(JBossXmlAdaptedType.class);
         if (adaptedType != null)
            generateAdaptedType(adaptedType);
         
         JBossXmlTransient[] xmlTransients = null;
         JBossXmlTransients transientsAnnotation = packageInfo.getUnderlyingAnnotation(JBossXmlTransients.class);
         if(transientsAnnotation == null)
         {
            JBossXmlTransient transientAnnotation = packageInfo.getUnderlyingAnnotation(JBossXmlTransient.class);
            if(transientAnnotation != null)
               xmlTransients = new JBossXmlTransient[]{transientAnnotation};
         }
         else
            xmlTransients = transientsAnnotation.value();

         if(xmlTransients != null)
         {
            jbossXmlTransients = new HashMap<String, Set<String>>();
            for(JBossXmlTransient xmlTransient : xmlTransients)
            {
               Set<String> properties;
               if(xmlTransient.properties().length == 0)
                  properties = Collections.emptySet();
               else
                  properties= new HashSet<String>(Arrays.asList(xmlTransient.properties()));
               jbossXmlTransients.put(xmlTransient.type().getName(), properties);
               if(trace)
                  log.trace("JBossXmlTransient type=" + xmlTransient.type().getName() + ", properties=" + properties);
            }
         }
      }
      
      if(jbossXmlSchema != null)
         beanAccessMode = jbossXmlAccessModeToBeanAccessMode(jbossXmlSchema.accessMode());
   }

   private static BeanAccessMode jbossXmlAccessModeToBeanAccessMode(JBossXmlAccessMode accessMode)
   {
      if (accessMode == JBossXmlAccessMode.ALL)
         return BeanAccessMode.ALL;
      else if (accessMode == JBossXmlAccessMode.PROPERTY)
         return BeanAccessMode.STANDARD;
      else if (accessMode == JBossXmlAccessMode.PUBLIC_MEMBER)
         return BeanAccessMode.FIELDS;
      throw new IllegalArgumentException("Unsupported JBossXmlAccessMode: " + accessMode);
   }
   
   /**
    * Create the root elements
    */
   protected void createRootElements()
   {
      // Create the root element
      createRootElementBinding(root);
   }

   /**
    * Create a root element binding
    * 
    * @param typeInfo the type info
    */
   protected void createRootElementBinding(TypeInfo typeInfo)
   {
      // Already done/doing this
      if (rootCache.containsKey(typeInfo))
         return;
      // Put a skeleton marker in the cache so we know not to redo it
      rootCache.put(typeInfo, null);

      // We force the element to be a root element
      push(typeInfo);
      try
      {
         createElementBinding(typeInfo, typeInfo.getSimpleName(), true);
         pop();
      }
      catch (Exception e)
      {
         throw rethrowWithLocation(e);
      }
   }

   /**
    * Create an element binding
    * 
    * @param typeInfo the type info
    * @param name the java element name
    * @param root pass true to force a root element
    * @return the element binding
    */
   private ElementBinding createElementBinding(TypeInfo typeInfo, String name, boolean root)
   {
      // Resolve the type
      TypeBinding typeBinding = resolveTypeBinding(typeInfo);

      // Create the element
      return createElementBinding(typeInfo, typeBinding, name, root);
   }

   /**
    * Create an element binding
    * 
    * @param typeInfo the type info
    * @param typeBinding the type binding
    * @param name the java element name
    * @param root pass true to force a root element
    * @return the element binding
    */
   private ElementBinding createElementBinding(TypeInfo typeInfo, TypeBinding typeBinding, String name, boolean root)
   {
      // Determine the parameters
      String overrideNamespace = null;
      String overrideName = null;
      if (typeInfo instanceof ClassInfo)
      {
         ClassInfo classInfo = (ClassInfo) typeInfo;
         XmlRootElement xmlRootElement = classInfo.getUnderlyingAnnotation(XmlRootElement.class);
         if (xmlRootElement != null)
         {
            overrideNamespace = xmlRootElement.namespace();
            overrideName = xmlRootElement.name();
         }
      }

      // Create the binding
      XmlNsForm form = elementForm;
      if (root)
         form = XmlNsForm.QUALIFIED;
      QName qName = generateXmlName(name, form, overrideNamespace, overrideName);
      return createElementBinding(typeInfo, typeBinding, qName, root);
   }

   /**
    * Create an element binding
    * 
    * @param typeInfo the type info
    * @param typeBinding the type binding
    * @param qName the qualified name
    * @param root pass true to force a root element
    * @return the element binding
    */
   private ElementBinding createElementBinding(TypeInfo typeInfo, TypeBinding typeBinding, QName qName, boolean root)
   {
      if (trace)
         log.trace("creating element " + qName + " with type " + typeInfo.getName());

      if (typeInfo instanceof ClassInfo)
      {
         ClassInfo classInfo = (ClassInfo) typeInfo;
         XmlRootElement xmlRootElement = classInfo.getUnderlyingAnnotation(XmlRootElement.class);
         if (xmlRootElement != null)
            root = true;
      }

      ElementBinding elementBinding = new ElementBinding(schemaBinding, qName, typeBinding);
      if (trace)
         log.trace("created  element " + qName + " element=" + elementBinding + " rootElement=" + root);

      // If we are a root element bind it
      if (root)
      {
         schemaBinding.addElement(elementBinding);
         ParticleBinding particleBinding = schemaBinding.getElementParticle(qName);
         particleBinding.setMinOccurs(1);
         particleBinding.setMaxOccurs(1);
         rootCache.put(typeInfo, elementBinding);
      }

      return elementBinding;
   }

   /**
    * Process a type
    * 
    * @param typeInfo the type info
    */
   protected void process(TypeInfo typeInfo)
   {
      if (typeInfo.isPrimitive() == false && typeInfo.isEnum() && typeInfo.isAnnotation() && Object.class.getName().equals(typeInfo.getName()) == false)
      {
         ClassInfo classInfo = (ClassInfo) typeInfo;

         // Create the type
         resolveTypeBinding(typeInfo);

         // Check wether we need to add it as a root element
         if (rootCache.containsKey(typeInfo) == false)
         {
            XmlRootElement xmlRootElement = classInfo.getUnderlyingAnnotation(XmlRootElement.class);
            if (xmlRootElement != null)
               createRootElementBinding(typeInfo);
         }
      }
   }

   /**
    * Resolve a type binding
    *
    * @param typeInfo the type info
    * @return the type binding
    */
   protected TypeBinding resolveTypeBinding(TypeInfo typeInfo)
   {
      if (trace)
         log.trace("resolving type " + typeInfo.getName());

      // Look for a cached value
      TypeBinding result = typeCache.get(typeInfo);

      // No cached value
      if (result == null)
      {
         // Generate it
         result = generateTypeBinding(typeInfo);

         // Cache it
         typeCache.put(typeInfo, result);
      }
      if (trace)
         log.trace("resolved  type " + typeInfo.getName() + " binding=" + result);

      // Return the result 
      return result;
   }

   /**
    * Generate a type binding
    * 
    * @param typeInfo the type info
    * @return the type binding
    */
   protected TypeBinding generateTypeBinding(TypeInfo typeInfo)
   {
      try
      {
         if (typeInfo.isEnum())
            return generateEnum((EnumInfo) typeInfo);

         if (typeInfo.isAnnotation())
            return generateAnnotation((ClassInfo) typeInfo);

         if (typeInfo.isArray())
            return generateArray((ArrayInfo) typeInfo);

         if (typeInfo.isCollection())
            return generateCollection((ClassInfo) typeInfo);

         if (typeInfo.isMap())
            return generateMap((ClassInfo) typeInfo);

         TypeBinding typeBinding = isSimpleType(typeInfo);
         if (typeBinding != null)
            return typeBinding;

         return generateBean((ClassInfo) typeInfo);
      }
      finally
      {
         // Not a primitive type
         if (typeInfo.isPrimitive() == false)
         {
            ClassInfo classInfo = (ClassInfo) typeInfo;

            // Process our type args
            TypeInfo[] typeArgs = classInfo.getActualTypeArguments();
            if (typeArgs != null)
            {
               for (int i = 0; i < typeArgs.length; ++i)
                  process(typeArgs[i]);
            }

            // Process the super class
            ClassInfo superClass = classInfo.getGenericSuperclass();
            if (superClass != null)
               process(superClass);
         }
      }
   }

   /**
    * Generate an enum type binding
    * 
    * @param typeInfo the type info
    * @return the type binding
    */
   public TypeBinding generateEnum(EnumInfo typeInfo)
   {
      // Determine the parameters
      String overrideNamespace = null;
      String overrideName = null;
      boolean root = false;
      XmlType xmlType = typeInfo.getUnderlyingAnnotation(XmlType.class);
      if (xmlType != null)
      {
         root = true;
         overrideNamespace = xmlType.namespace();
         overrideName = xmlType.name();
      }

      // Determine the enum type 
      Class<?> xmlEnumValue = String.class;
      XmlEnum xmlEnum = typeInfo.getUnderlyingAnnotation(XmlEnum.class);
      if (xmlEnum != null)
         xmlEnumValue = xmlEnum.value();
      TypeInfo enumType = typeInfo.getTypeInfoFactory().getTypeInfo(xmlEnumValue);

      // Resolve the enum type as the parent (must be simple)
      TypeBinding parent = getSimpleType(enumType);

      // Create the enum type
      QName qName = null;
      TypeBinding typeBinding = null;
      if (root)
      {
         qName = generateXmlName(typeInfo, XmlNsForm.QUALIFIED, overrideNamespace, overrideName);
         typeBinding = new TypeBinding(qName, parent);
      }
      else
      {
         typeBinding = new TypeBinding(null, parent);
      }

      typeBinding.setValueAdapter(new EnumValueAdapter(qName, typeInfo, enumType));

      if (trace)
         log.trace("Created enum=" + typeInfo.getName() + " type=" + typeBinding + " rootType=" + root);

      // Bind it as a global type
      if (root)
         schemaBinding.addType(typeBinding);
      else
         typeBinding.setSchemaBinding(schemaBinding);

      return typeBinding;
   }

   /**
    * Generate an adapted type
    * 
    * @param adaptedType the information about the adaption
    * @return the type binding
    */
   public TypeBinding generateAdaptedType(JBossXmlAdaptedType adaptedType)
   {
      if(adaptedType.type() == JBossXmlConstants.DEFAULT.class)
         throw new JBossXBRuntimeException("@JBossXmlAdaptedType used in package-info.java must specify type element.");
      
      // Determine the parameters
      String overrideNamespace = adaptedType.namespace();
      String overrideName = adaptedType.name();
      Class<?> type = adaptedType.type();
      try
      {
         TypeInfo typeInfo = JBossXBBuilder.configuration.getTypeInfo(type);

         QName qName = generateXmlName(typeInfo, XmlNsForm.QUALIFIED, overrideNamespace, overrideName);

         TypeInfo parentType = typeInfo.getTypeInfoFactory().getTypeInfo(String.class);
         TypeBinding parent = getSimpleType(parentType);
         TypeBinding typeBinding = new TypeBinding(qName, parent);

         adaptType(typeBinding, adaptedType);

         typeCache.put(typeInfo, typeBinding);
         schemaBinding.addType(typeBinding);

         return typeBinding;
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Unable to adapt type " + type.getName() + " with " + adaptedType.valueAdapter().getName(), t);
      }
   }

   /**
    * Generate an adapted type
    * 
    * @param adaptedType the information about the adaption
    * @return the type binding
    */
   public void adaptType(TypeBinding adaptedType, JBossXmlAdaptedType annotation)
   {
      // Determine the parameters
      Class<?> type = annotation.type();
      Class<? extends ValueAdapter> adapter = annotation.valueAdapter();
      try
      {
         BeanInfo adapterInfo = JBossXBBuilder.configuration.getBeanInfo(adapter, beanAccessMode);

         ValueAdapter valueAdapter = (ValueAdapter) adapterInfo.newInstance();

         adaptedType.setValueAdapter(valueAdapter);
         if (trace)
            log.trace("adapted typeBinding=" + adaptedType + " adapter=" + adapter.getName());
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Unable to adapt type " + type.getName() + " with " + adapter.getName(), t);
      }
   }

   /**
    * Generate an annotation type binding
    * 
    * @param typeInfo the type info
    * @return the type binding
    */
   public TypeBinding generateAnnotation(ClassInfo typeInfo)
   {
      // TODO generateAnnotation
      throw new UnsupportedOperationException("generateAnnotation");
   }

   /**
    * Generate an array type binding
    * 
    * @param typeInfo the type info
    * @return the type binding
    */
   public TypeBinding generateArray(ArrayInfo typeInfo)
   {
      return resolveTypeBinding(typeInfo.getComponentType());
   }

   /**
    * Generate a collection type binding
    * 
    * @param typeInfo the type info
    * @return the type binding
    */
   public TypeBinding generateCollection(ClassInfo typeInfo)
   {
      TypeInfo[] actualTypes = typeInfo.getActualTypeArguments();
      if (actualTypes == null || actualTypes.length == 0)
         return generateBean(typeInfo);

      TypeInfo elementType = actualTypes[0];
      return resolveTypeBinding(elementType);
   }

   /**
    * Generate a map type binding
    * 
    * @param typeInfo the type info
    * @return the type binding
    */
   public TypeBinding generateMap(ClassInfo typeInfo)
   {
      // the map is bound in bindProperty method currently
      return generateBean(typeInfo);
   }

   /**
    * Check whether this is a simple type
    * 
    * @param typeInfo the type info
    * @return the type binding if it is simple
    */
   public TypeBinding isSimpleType(TypeInfo typeInfo)
   {
      QName qName = SimpleTypeBindings.typeQName(typeInfo.getType());
      if (qName == null)
         return null;
      TypeBinding result = schemaBinding.getType(qName);
      if (result == null)
         throw new IllegalStateException("SimpleType is not bound in the schema: " + qName + " for " + typeInfo.getName());
      result.setHandler(BuilderSimpleParticleHandler.SIMPLE_INSTANCE);
      return result;
   }

   /**
    * Get the simple type
    * 
    * @param typeInfo the type info
    * @return the type binding if it is simple
    * @throws IllegalStateException if the type is not bound
    */
   public TypeBinding getSimpleType(TypeInfo typeInfo)
   {
      TypeBinding result = isSimpleType(typeInfo);
      if (result == null)
         throw new IllegalStateException(typeInfo.getName() + " does not map to a simple type.");
      return result;
   }

   /**
    * Generate a bean type binding
    * 
    * @param typeInfo the type info
    * @return the type binding
    */
   public TypeBinding generateBean(ClassInfo typeInfo)
   {
      return generateBean(typeInfo, false);
   }

   /**
    * Generate a bean type binding
    * 
    * @param typeInfo the type info
    * @param root whether to force a root type
    * @return the type binding
    */
   public TypeBinding generateBean(ClassInfo typeInfo, boolean root)
   {
      return generateType(typeInfo, root);
   }

   /**
    * Generate a bean type binding
    * 
    * @param typeInfo the type info
    * @param root whether to force a root type
    * @return the type binding
    */
   public TypeBinding generateType(ClassInfo typeInfo, boolean root)
   {
      // Determine the parameters
      String overrideNamespace = null;
      String overrideName = null;
      ClassInfo factoryClassInfo = typeInfo;
      String factoryMethod = null;
      String[] propertyOrder = {""};
      XmlAccessOrder accessOrder = XmlAccessOrder.UNDEFINED;
      Class<? extends BeanAdapterBuilder> beanAdapterBuilderClass = DefaultBeanAdapterBuilder.class;
      XmlType xmlType = typeInfo.getUnderlyingAnnotation(XmlType.class);
      if (xmlType != null)
      {
         root = true;
         overrideNamespace = xmlType.namespace();
         overrideName = xmlType.name();
         if (overrideName.length() == 0)
            root = false;

         Class<?> factoryClass = xmlType.factoryClass();
         if (factoryClass != XmlType.DEFAULT.class)
            factoryClassInfo = (ClassInfo) typeInfo.getTypeInfoFactory().getTypeInfo(factoryClass);
         factoryMethod = xmlType.factoryMethod();
         propertyOrder = xmlType.propOrder();
      }
      
      BeanAccessMode beanAccessMode = this.beanAccessMode;
      JBossXmlType jbossXmlType = typeInfo.getUnderlyingAnnotation(JBossXmlType.class);
      
      if (jbossXmlType != null)
      {
         beanAdapterBuilderClass = jbossXmlType.beanAdapterBuilder();
         JBossXmlAccessMode accessMode = jbossXmlType.accessMode();
         if(accessMode != JBossXmlAccessMode.NOT_SPECIFIED)
            beanAccessMode = jbossXmlAccessModeToBeanAccessMode(accessMode);
      }
      // Determine the property access order
      XmlAccessorOrder accessorOrder = typeInfo.getUnderlyingAnnotation(XmlAccessorOrder.class);
      if (accessorOrder == null)
      {
         PackageInfo pkg = typeInfo.getPackage();
         if (pkg != null)
            accessorOrder = pkg.getUnderlyingAnnotation(XmlAccessorOrder.class);
      }
      if (accessorOrder != null)
         accessOrder = accessorOrder.value();

      // Create the binding
      TypeBinding typeBinding = null;

      if (root)
      {
         QName qName = generateXmlName(typeInfo, XmlNsForm.QUALIFIED, overrideNamespace, overrideName);
         typeBinding = new TypeBinding(qName, CharactersHandler.NOOP);
         schemaBinding.addType(typeBinding);
      }
      else
      {
         typeBinding = new TypeBinding();
      }
      typeBinding.setSchemaBinding(schemaBinding);

      // Push into the cache early to avoid recursion
      typeCache.put(typeInfo, typeBinding);

      JBossXmlAdaptedType adaptedType = typeInfo.getUnderlyingAnnotation(JBossXmlAdaptedType.class);
      if(adaptedType != null)
      {
         if(adaptedType.type() != JBossXmlConstants.DEFAULT.class)
            throw new JBossXBRuntimeException("@JBossXmlAdaptedType on a type must not specify type element: " + typeInfo.getName());
         adaptType(typeBinding, adaptedType);
      }

      // Determine any factory method
      MethodInfo factory = null;
      if (factoryMethod != null && factoryMethod.length() > 0)
         factory = Config.findMethodInfo(factoryClassInfo, factoryMethod, null, true, true);

      // Create the handler
      BeanInfo beanInfo = JBossXBBuilder.configuration.getBeanInfo(typeInfo, beanAccessMode);
      BeanAdapterFactory beanAdapterFactory = createAdapterFactory(beanAdapterBuilderClass, beanInfo, factory);
      BeanHandler handler = new BeanHandler(beanInfo.getName(), beanAdapterFactory, typeBinding);
      typeBinding.setHandler(handler);
      if (trace)
         log.trace("Created BeanHandler for type=" + beanInfo.getName() + " factory=" + factory);

      // Look through the properties
      JBossXmlNoElements jbossXmlNoElements = typeInfo.getUnderlyingAnnotation(JBossXmlNoElements.class);
      boolean noElements = jbossXmlNoElements != null;
      PropertyInfo valueProperty = null;
      PropertyInfo wildcardProperty = null;
      boolean allBinding = propertyOrder.length == 0;
      boolean determinePropertyOrder = allBinding || (propertyOrder.length == 1 && propertyOrder[0].length() == 0);
      ArrayList<String> propertyNames = new ArrayList<String>();
      Set<PropertyInfo> properties = beanInfo.getProperties();
      if (properties != null && properties.isEmpty() == false)
      {
         boolean seenXmlAnyElement = false;
         PropertyInfo seenXmlAnyAttribute = null;
         for (PropertyInfo property : properties)
         {
            push(typeInfo, property.getName());

            if (trace)
               log.trace("Checking property " + property.getName() + " for " + beanInfo.getName() + " type=" + property.getType().getName());

            // Is this the value property?
            XmlValue xmlValue = property.getUnderlyingAnnotation(XmlValue.class);
            if (xmlValue != null)
            {
               if (trace)
                  log.trace("Seen @XmlValue for type=" + beanInfo.getName() + " property=" + property.getName());
               if (valueProperty != null)
                  throw new RuntimeException("@XmlValue seen on two properties: " + property.getName() + " and " + valueProperty.getName());
               valueProperty = property;
            }

            // Is this the wildcard property?
            boolean ignoreXmlAnyElement = false;
            XmlAnyElement xmlAnyElement = property.getUnderlyingAnnotation(XmlAnyElement.class);
            if (xmlAnyElement != null)
            {
               if (trace)
                  log.trace("Seen @XmlAnyElement for type=" + beanInfo.getName() + " property=" + property.getName());
               if (wildcardProperty != null && seenXmlAnyElement)
                  throw new RuntimeException("@XmlAnyElement seen on two properties: " + property.getName() + " and " + wildcardProperty.getName());
               wildcardProperty = property;
               seenXmlAnyElement = true;
               
               // should we ignore it
               if(property.getUnderlyingAnnotation(XmlElements.class) == null &&
                  property.getUnderlyingAnnotation(XmlElementRefs.class) == null)
                  ignoreXmlAnyElement = true;
            }

            // Is this an attribute
            XmlAttribute xmlAttribute = property.getUnderlyingAnnotation(XmlAttribute.class);
            if (xmlAttribute != null)
            {
               JBossXmlAttribute jbossXmlAttribute = property.getUnderlyingAnnotation(JBossXmlAttribute.class);
               // Determine the name
               QName qName = generateXmlName(property.getName(), attributeForm, xmlAttribute.namespace(), xmlAttribute.name());
               // Resolve the type
               TypeInfo attributeTypeInfo = property.getType();
               if (jbossXmlAttribute != null && jbossXmlAttribute.type() != Object.class)
                  attributeTypeInfo = attributeTypeInfo.getTypeInfoFactory().getTypeInfo(jbossXmlAttribute.type());
               
               XBValueAdapter valueAdapter = null;
               XmlJavaTypeAdapter xmlTypeAdapter = property.getUnderlyingAnnotation(XmlJavaTypeAdapter.class);
               if (xmlTypeAdapter != null)
               {
                  valueAdapter = new XBValueAdapter(xmlTypeAdapter.value(), attributeTypeInfo.getTypeInfoFactory());
                  attributeTypeInfo = valueAdapter.getAdaptedTypeInfo();
               }

               TypeBinding attributeType = resolveTypeBinding(attributeTypeInfo);
               
               // Create the attribute handler
               AttributeHandler attributeHandler = null;
               if(attributeTypeInfo.isCollection() || attributeTypeInfo.isArray())
               {
                  TypeBinding itemType = attributeType;
                  attributeType = new SimpleTypeBinding(null);
                  attributeType.setSchemaBinding(schemaBinding);
                  attributeType.setItemType(itemType);
               }

               attributeHandler = new PropertyHandler(property, attributeTypeInfo);
               
               // Create the attribute and bind it to the type
               AttributeBinding attribute = new AttributeBinding(schemaBinding, qName, attributeType, attributeHandler);
               if(valueAdapter != null)
                  attribute.setValueAdapter(valueAdapter);
               attribute.setRequired(xmlAttribute.required());
               typeBinding.addAttribute(attribute);
               JBossXmlPreserveWhitespace preserveSpace = property.getUnderlyingAnnotation(JBossXmlPreserveWhitespace.class);
               if(preserveSpace != null)
                  attribute.setNormalizeSpace(preserveSpace.preserve() ? false : true);
               if (trace)
                  log.trace("Bound attribute " + qName + " type=" + beanInfo.getName() + " property=" + property.getName() + " propertyType=" + attributeTypeInfo + ", normalizeSpace=" + attribute.isNormalizeSpace() + ", typeBinding=" + typeBinding.getQName());
               
               handler.getAttributesHandler().addAttribute(attribute);
            }

            // Is this any attribute
            XmlAnyAttribute xmlAnyAttribute = property.getUnderlyingAnnotation(XmlAnyAttribute.class);
            if (xmlAnyAttribute != null)
            {
               if (seenXmlAnyAttribute != null)
                  throw new RuntimeException("@XmlAnyAttribute seen on two properties: " + property.getName() + " and " + seenXmlAnyAttribute.getName());
               seenXmlAnyAttribute = property;
               
               AnyAttributePropertyHandler anyHandler = new AnyAttributePropertyHandler(property, property.getType());
               AnyAttributeBinding anyAttribute = new AnyAttributeBinding(schemaBinding, anyHandler);
               typeBinding.setAnyAttribute(anyAttribute);

               JBossXmlPreserveWhitespace preserveSpace = property.getUnderlyingAnnotation(JBossXmlPreserveWhitespace.class);
               if(preserveSpace != null)
                  anyAttribute.setNormalizeSpace(preserveSpace.preserve() ? false : true);
               if (trace)
                  log.trace("Bound any attribute type=" + beanInfo.getName() + " property=" + property.getName() + ", normalizeSpace=" + anyAttribute.isNormalizeSpace());
               
               handler.getAttributesHandler().setAnyAttribute(anyAttribute);
            }
            
            // Are we determining the property order?
            if (determinePropertyOrder)
            {
               // Value property
               if (xmlValue != null)
               {
                  if (trace)
                     log.trace("Ignore not element @XmlValue for type=" + beanInfo.getName() + " property=" + property.getName());
                  pop();
                  continue;
               }
               // Wildcard property
               if (ignoreXmlAnyElement)
               {
                  if (trace)
                     log.trace("Ignore not element @XmlAnyElement for type=" + beanInfo.getName() + " property=" + property.getName());
                  pop();
                  continue;
               }
               // Ignore xml attribute
               if (xmlAttribute != null)
               {
                  if (trace)
                     log.trace("Ignore not element @XmlAttribute for type=" + beanInfo.getName() + " property=" + property.getName());
                  pop();
                  continue;
               }
               // Ignore xml tranient
               XmlTransient xmlTransient = property.getUnderlyingAnnotation(XmlTransient.class);
               if (xmlTransient != null)
               {
                  if (trace)
                     log.trace("Ignore not element @XmlTransient for type=" + beanInfo.getName() + " property=" + property.getName());
                  pop();
                  continue;
               }
               // Ignore the class property
               String name = property.getName();
               if ("class".equals(name))
               {
                  pop();
                  continue;
               }

               if (noElements)
               {
                  pop();
                  continue;
               }

               ClassInfo declaringClass;
               MethodInfo methodInfo = property.getGetter();
               if(methodInfo == null)
               {
                  FieldInfo fieldInfo = property.getFieldInfo();
                  if(fieldInfo == null)
                  {
                     methodInfo = property.getSetter();
                     if(methodInfo == null)
                        throw new JBossXBRuntimeException("Couldn't get access to getter, setter or field info for type=" + beanInfo.getName() + " property=" + property.getName());
                     else
                        declaringClass = methodInfo.getDeclaringClass();
                  }
                  else
                     declaringClass = fieldInfo.getDeclaringClass();
               }
               else
                  declaringClass = methodInfo.getDeclaringClass();
               
               Set<String> transientProps = jbossXmlTransients.get(declaringClass.getName());
               if(transientProps != null && (transientProps.isEmpty() || transientProps.contains(property.getName())))
               {
                  if(trace)
                     log.trace("Ignore JBossXmlTransient property for type=" + beanInfo.getName() + " property=" + property.getName());
                  pop();
                  continue;
               }

               if (trace)
                  log.trace("Element for type=" + beanInfo.getName() + " property=" + property.getName());
               propertyNames.add(property.getName());
            }

            pop();
         }
         // Apply any access order
         if (determinePropertyOrder)
         {
            if (accessOrder == XmlAccessOrder.ALPHABETICAL)
               Collections.sort(propertyNames);
            propertyOrder = propertyNames.toArray(new String[propertyNames.size()]);
         }
      }

      // Bind the value
      if (valueProperty != null)
      {
         CharactersHandler charactersHandler = new ValueHandler(valueProperty);
         typeBinding.setSimpleType(charactersHandler);
         
         JBossXmlValue jbossXmlValue = typeInfo.getUnderlyingAnnotation(JBossXmlValue.class);
         if(jbossXmlValue != null)
         {
            if(trace)
               log.trace("Type " + typeInfo.getName() + " is annotated with @JBossXmlValue.ignoreEmptyString=" + jbossXmlValue.ignoreEmptyString());
            typeBinding.setIgnoreEmptyString(jbossXmlValue.ignoreEmptyString());
         }
      }
      else if (trace)
         log.trace("No value for type=" + beanInfo.getName());

      if (trace)
         log.trace("PropertyOrder " + Arrays.asList(propertyOrder) + " for type=" + beanInfo.getName());

      // Determine the model
      // TODO simple types/content when no properties other than @XmlValue and @XmlAttribute
      typeBinding.setSimple(false);
      ModelGroupBinding model = null;
      boolean propOrderMissing = propertyNames.size() > 1 && determinePropertyOrder && accessOrder == XmlAccessOrder.UNDEFINED;
      if(jbossXmlType != null && !JBossXmlConstants.DEFAULT.equals(jbossXmlType.modelGroup()))
         model = createModelGroup(jbossXmlType.modelGroup(), typeInfo, propOrderMissing, propertyOrder, null);
      else if (allBinding)
         model = new AllBinding(schemaBinding);
      else
      {
         if(propOrderMissing)
            assertPropOrderNotRequired(typeInfo, propertyOrder);
         model = groupFactory.createSequence(schemaBinding);
      }

      boolean previousRepeatableHandlers = this.currentRepeatableHandlers;
      if(model instanceof UnorderedSequenceBinding)
         this.currentRepeatableHandlers = false;
      else
         this.currentRepeatableHandlers = this.repeatableParticleHandlers;

      if (trace)
         log.trace(model.getGroupType() + " model group for type=" + beanInfo.getName());

      model.setHandler(BuilderParticleHandler.setParentDelegate(typeBinding.getHandler()));
      ParticleBinding typeParticle = new ParticleBinding(model);
      typeParticle.setMinOccurs(1);
      typeParticle.setMaxOccurs(1);
      typeBinding.setParticle(typeParticle);

      if (typeInfo.isCollection())
      {
         typeParticle.setMinOccurs(0);
         typeParticle.setMaxOccursUnbounded(true);
      }

      // Look through the properties
      for (String name : propertyOrder)
      {
         // propertyOrder is initialized to {""}
         if(name.length() == 0)
            continue;
         // Setup the error stack
         push(typeInfo, name);
         // Get the property
         PropertyInfo property = beanInfo.getProperty(name);
         bindProperty(property, model, beanAdapterFactory, propertyOrder, property == wildcardProperty);
         pop();
      }

      // Bind the children
      JBossXmlChild[] children = null;
      JBossXmlChildren jbossXmlChildren = typeInfo.getUnderlyingAnnotation(JBossXmlChildren.class);
      if (jbossXmlChildren != null)
         children = jbossXmlChildren.value();
      else
      {
         JBossXmlChild jbossXmlChild = typeInfo.getUnderlyingAnnotation(JBossXmlChild.class);
         if (jbossXmlChild != null)
            children = new JBossXmlChild[] { jbossXmlChild };
      }

      if (children != null && children.length > 0)
      {
         for (JBossXmlChild child : children)
         {
            QName qName = generateXmlName(child.name(), elementForm, child.namespace(), child.name());
            TypeInfo childType = JBossXBBuilder.configuration.getTypeInfo(child.type());

            TypeBinding elementTypeBinding = resolveTypeBinding(childType);
            ElementBinding elementBinding = createElementBinding(childType, elementTypeBinding, qName, false);

            // Bind it to the model
            ParticleBinding particle = new ParticleBinding(elementBinding, child.minOccurs(), child.maxOccurs(), child.unbounded());
            model.addParticle(particle);

            if(childType.isMap())
               bindMapProperty(null, (ClassInfo) childType, elementTypeBinding.getQName(), (ModelGroupBinding) elementTypeBinding.getParticle().getTerm());
            
            DefaultElementInterceptor interceptor = null;
            if (typeInfo.isCollection())
               interceptor = ChildCollectionInterceptor.SINGLETON;
            else
            {
               // Expect a type with a value property to accept the child value
               PropertyInfo property = beanInfo.getProperty("value");
               if (property == null)
                  throw new UnsupportedOperationException("Expected a value property for non-collection type with JBossXmlChildren");
               TypeInfo propertyType = property.getType();
               interceptor = new PropertyInterceptor(property, propertyType);
            }
            typeBinding.pushInterceptor(qName, interceptor);
            if (trace)
               log.trace("Added interceptor " + qName + " for type=" + childType + " interceptor=" + interceptor);
         }
      }

      // Bind the wildcard
      if (wildcardProperty != null)
      {
         AbstractPropertyHandler wildcardHandler;
         WildcardBinding wildcard = Util.getWildcard(model);
         if(wildcard == null)
         {
            if (trace)
               log.trace("Processing WildcardProperty for type=" + beanInfo.getName() + " property=" + wildcardProperty.getName());
            ModelGroupBinding localModel = model;
            TypeInfo wildcardType = wildcardProperty.getType();
            TypeInfo type = wildcardType;

            wildcard = new WildcardBinding(schemaBinding);
            ParticleBinding particleBinding = new ParticleBinding(wildcard);
            localModel.addParticle(particleBinding);
            particleBinding.setMinOccurs(0);

            // Setup any new model and determine the wildcard type
            if (wildcardType.isArray())
            {
               particleBinding.setMaxOccursUnbounded(true);
               wildcardHandler = new PropertyWildcardHandler(wildcardProperty, wildcardType);
               if(currentRepeatableHandlers)
                  wildcard.setRepeatableHandler(new ArrayWrapperRepeatableParticleHandler(wildcardHandler));
               else
                  wildcard.setRepeatableHandler(new AppendingArrayRepeatableHandler(wildcardHandler));
               type = ((ArrayInfo) wildcardType).getComponentType();
               if (trace)
                  log.trace("Wildcard " + wildcardProperty.getName() + " is an array of type " + type.getName());
            }
            else if (wildcardType.isCollection())
            {
               particleBinding.setMaxOccursUnbounded(true);
               if(currentRepeatableHandlers)
               {
                  wildcardHandler = new PropertyWildcardHandler(wildcardProperty, wildcardType);
                  wildcard.setRepeatableHandler(new CollectionRepeatableParticleHandler(wildcardHandler, (ClassInfo) wildcardType, null));
               }
               else
                  wildcardHandler = new CollectionPropertyWildcardHandler(wildcardProperty, wildcardType);
               
               type = ((ClassInfo)wildcardProperty.getType()).getComponentType();
               if (trace)
                  log.trace("Wildcard " + wildcardProperty.getName() + " is a collection of type " + type.getName());
            }
            else
            {
               particleBinding.setMaxOccurs(1);
               wildcardHandler = new PropertyWildcardHandler(wildcardProperty, wildcardType);
            }

            wildcard.setHandler((ParticleHandler) wildcardHandler);

            XmlAnyElement xmlAnyElement = wildcardProperty.getUnderlyingAnnotation(XmlAnyElement.class);
            boolean isLax = xmlAnyElement == null ? true : xmlAnyElement.lax();
            if (isLax)
               wildcard.setProcessContents((short) 3); // Lax
            else
               wildcard.setProcessContents((short) 1); // Strict

            // Dom element?
            if (Element.class.getName().equals(type.getName()))
            {
               wildcard.setUnresolvedElementHandler(DOMHandler.INSTANCE);
               wildcard.setUnresolvedCharactersHandler(DOMHandler.INSTANCE);
            }            
         }
         else
            wildcardHandler = (AbstractPropertyHandler) wildcard.getHandler();
         
         beanAdapterFactory.setWildcardHandler(wildcardHandler);
      }

      JBossXmlChildWildcard childWildcard = typeInfo.getUnderlyingAnnotation(JBossXmlChildWildcard.class);
      if (childWildcard != null)
      {
         if (beanAdapterFactory.getWildcardHandler() != null)
            throw new RuntimeException("Cannot have both @JBossXmlChildWildcard and @XmlAnyElement");

         ParticleHandler childWildcardHandler = null;
         if (typeInfo.isCollection())
         {
            if (childWildcard.wrapper() != Object.class)
            {
               BeanInfo wrapperInfo = JBossXBBuilder.configuration.getBeanInfo(childWildcard.wrapper(), beanAccessMode);
               childWildcardHandler = new ChildCollectionWildcardHandler(wrapperInfo, childWildcard.property());
            }
            else
               childWildcardHandler = ChildCollectionWildcardHandler.SINGLETON;
         }
         else
            throw new UnsupportedOperationException("TODO");

         WildcardBinding wildcard = new WildcardBinding(schemaBinding);
         if (childWildcard.lax())
            wildcard.setProcessContents((short) 3); // Lax
         else
            wildcard.setProcessContents((short) 1); // Strict

         ParticleBinding particleBinding = new ParticleBinding(wildcard);
         particleBinding.setMinOccurs(0);
         particleBinding.setMaxOccurs(1);
         model.addParticle(particleBinding);

         typeBinding.getWildcard().setHandler(childWildcardHandler);
      }

      if (trace)
         log.trace("Created type=" + typeInfo.getName() + " typeBinding=" + typeBinding + " rootType=" + root);

      this.currentRepeatableHandlers = previousRepeatableHandlers;
      return typeBinding;
   }

   private void assertPropOrderNotRequired(TypeInfo typeInfo, String[] propertyOrder)
   {
      StringBuffer msg = new StringBuffer();
      msg.append("Property order is not specified for type ")
      .append(typeInfo.getName())
      .append(" bound to a sequence. Property order can be specified using @XmlType.propOrder or @XmlAccessorOrder.");
      if(propertyOrder != null && propertyOrder.length > 1)
      {
         msg.append(" List of properties:");
         for(String name : propertyOrder)
            msg.append(" ").append(name);
      }
      
      if(sequencesRequirePropOrder && !useUnorderedSequence)
         throw new JBossXBRuntimeException(msg.toString());
      //else log.warn(msg.toString()); this resulted in a lot of WARN logging in the AS
   }

   private void bindProperty(PropertyInfo property, ModelGroupBinding parentModel,
         BeanAdapterFactory beanAdapterFactory, String[] propertyOrder, boolean wildcardProperty)
   {
      TypeInfo propertyType = property.getType();
      if (trace)
         log.trace("Processing type=" + property.getBeanInfo().getName() + " property=" + property.getName());

      // This is illegal
      XmlTransient xmlTransient = property.getUnderlyingAnnotation(XmlTransient.class);
      if (xmlTransient != null && propertyOrder != null)
         throw new RuntimeException("Property " + property.getName() + " in property order "
               + Arrays.asList(propertyOrder) + " is marked @XmlTransient");
      
      // The current model
      ModelGroupBinding localModel = parentModel;

      TypeInfo propertyComponentType = propertyType;
      XmlType propertyXmlType = null;
      JBossXmlModelGroup propertyXmlModelGroup = null;
      // Setup any new model
      if (propertyType.isCollection() || propertyType.isArray())
      {
         if (trace)
            log.trace("Property " + property.getName() + " is a collection");
         JBossXmlCollection xmlCol = property.getUnderlyingAnnotation(JBossXmlCollection.class);
         if (xmlCol != null)
         {
            // this is the type that should be analyzed
            propertyType = propertyType.getTypeInfoFactory().getTypeInfo(xmlCol.type());
         }
         ClassInfo propertyClassInfo = (ClassInfo)propertyType;
         propertyXmlType = propertyClassInfo.getUnderlyingAnnotation(XmlType.class);
         propertyComponentType = propertyClassInfo.getComponentType();
      }

      // Is this property bound to a model group
      if (!propertyComponentType.isPrimitive())
      {
         ClassInfo componentClass = (ClassInfo) propertyComponentType;

         // TODO XmlElement on this property?..
         //XmlElement propXmlElement = property.getUnderlyingAnnotation(XmlElement.class);
         //if (propXmlElement != null)
         //   propClassInfo = (ClassInfo) propClassInfo.getTypeInfoFactory().getTypeInfo(propXmlElement.type());
         
         // if it's a model group then 
         propertyXmlModelGroup = componentClass.getUnderlyingAnnotation(JBossXmlModelGroup.class);
         if (propertyXmlType == null && propertyXmlModelGroup != null)
         {
            // model group value handler based on the model group name
            // TODO what if it doesn't have a name?
            AbstractPropertyHandler propertyHandler = null;
            if (propertyType.isCollection())
               propertyHandler = new CollectionPropertyHandler(property, propertyType);
            else
               propertyHandler = new PropertyHandler(property, propertyType);
            bindModelGroup(propertyXmlModelGroup, property, beanAdapterFactory, propertyHandler, null, localModel);
            return;
         }
      }

      // So this is element(s)
      XmlElement[] elements = null;
      XmlElement xmlElement = property.getUnderlyingAnnotation(XmlElement.class);
      if (xmlElement != null)
      {
         // A single element annotated
         elements = new XmlElement[]{xmlElement};
      }
      else
      {
         // Mutlple elements
         XmlElements xmlElements = property.getUnderlyingAnnotation(XmlElements.class);
         if (xmlElements != null)
            elements = xmlElements.value();
      }

      // A single element not annotated
      if (elements == null || elements.length == 0)
         elements = new XmlElement[1];

      // support for @XmlElementWrapper
      // the wrapping element is ignored in this case
      XmlElementWrapper xmlWrapper = property.getUnderlyingAnnotation(XmlElementWrapper.class);
      if (xmlWrapper != null)
      {         
         String wrapperNamespace = xmlWrapper.namespace();
         String wrapperName = xmlWrapper.name();
         QName wrapperQName = generateXmlName(property.getName(), elementForm, wrapperNamespace, wrapperName);
         PropertyHandler setWrapperProperty = new PropertyHandler(property, propertyType);
         beanAdapterFactory.addProperty(wrapperQName, setWrapperProperty);
         localModel = bindXmlElementWrapper(setWrapperProperty, propertyType, localModel, xmlWrapper, wrapperQName);
         if (trace)
            log.trace("Added property " + wrapperQName + " for type=" + property.getBeanInfo().getName() + " property="
                  + property.getName() + " as a wrapper element");
      }

      // Setup a choice
      boolean repeatableChoice = false;
      if (elements.length > 1)
      {
         ChoiceBinding choice = new ChoiceBinding(schemaBinding);
         choice.setHandler(BuilderParticleHandler.parentGroup(localModel));
         ParticleBinding particleBinding = new ParticleBinding(choice);
         particleBinding.setMinOccurs(0);
         // WARN normally maxOccursUnbounded should be set to true in this case
         // but I make an exception for case like in org.jboss.test.xb.builder.repeatableterms.support.Sequence
         if(propertyType.isCollection() || propertyType.isArray())
         {
            particleBinding.setMaxOccursUnbounded(true);
            repeatableChoice = true;
         }
         localModel.addParticle(particleBinding);
         localModel = choice;

         if(xmlWrapper == null)
         {
            if(propertyType.isArray())
            {
               if(currentRepeatableHandlers)
                  choice.setRepeatableHandler(new ArrayWrapperRepeatableParticleHandler(new PropertyHandler(property, propertyType)));
               else
                  choice.setRepeatableHandler(new AppendingArrayRepeatableHandler(new PropertyHandler(property, propertyType)));
            }
            else if(currentRepeatableHandlers && propertyType.isCollection())
               choice.setRepeatableHandler(new CollectionRepeatableParticleHandler(new PropertyHandler(property, propertyType), (ClassInfo) propertyType, null));
         }

         if (trace)
            log.trace("XmlElements seen adding choice for type=" + property.getBeanInfo().getName() + " property=" + property.getName());
      }

      // Bind the wildcard
      if (wildcardProperty)
      {
         if (trace)
            log.trace("Processing WildcardProperty for property=" + property.getName());

         WildcardBinding wildcard = new WildcardBinding(schemaBinding);
         ParticleBinding particleBinding = new ParticleBinding(wildcard);
         localModel.addParticle(particleBinding);
         particleBinding.setMinOccurs(0);

         AbstractPropertyHandler wildcardHandler;

         // Setup any new model and determine the wildcard type
         TypeInfo wildcardType = propertyType;
         if (propertyType.isArray())
         {
            if(!repeatableChoice)
               particleBinding.setMaxOccursUnbounded(true);
            wildcardHandler = new PropertyWildcardHandler(property, propertyType);
            // this is actually currently not kicking in because most probably it's a choice (XmlElements)
            // and the choice's repeatable handler will be used instead
            wildcard.setRepeatableHandler(new ArrayWrapperRepeatableParticleHandler(wildcardHandler));
            wildcardType = ((ArrayInfo) propertyType).getComponentType();
            if (trace)
               log.trace("Wildcard " + property.getName() + " is an array of type " + wildcardType.getName());
         }
         else if (propertyType.isCollection())
         {
            if(!repeatableChoice)
               particleBinding.setMaxOccursUnbounded(true);
            if(currentRepeatableHandlers)
            {
               wildcardHandler = new PropertyWildcardHandler(property, propertyType);
               // this is actually currently not kicking in because most probably it's a choice (XmlElements)
               // and the choice's repeatable handler will be used instead
               wildcard.setRepeatableHandler(new CollectionRepeatableParticleHandler(wildcardHandler, (ClassInfo) propertyType, null));
            }
            else
               wildcardHandler = new CollectionPropertyWildcardHandler(property, propertyType);

            wildcardType = ((ClassInfo)property.getType()).getComponentType();
            if (trace)
               log.trace("Wildcard " + property.getName() + " is a collection of type " + wildcardType.getName());
         }
         else
         {
            wildcardHandler = new PropertyWildcardHandler(property, propertyType);
            particleBinding.setMaxOccurs(1);
         }

         XmlAnyElement xmlAnyElement = property.getUnderlyingAnnotation(XmlAnyElement.class);
         boolean isLax = xmlAnyElement == null ? true : xmlAnyElement.lax();
         if (isLax)
            wildcard.setProcessContents((short) 3); // Lax
         else
            wildcard.setProcessContents((short) 1); // Strict

         // Dom element?
         if (Element.class.getName().equals(wildcardType.getName()))
         {
            wildcard.setUnresolvedElementHandler(DOMHandler.INSTANCE);
            wildcard.setUnresolvedCharactersHandler(DOMHandler.INSTANCE);
         }

         wildcard.setHandler((ParticleHandler) wildcardHandler);
         beanAdapterFactory.setWildcardHandler(wildcardHandler);
      }

      String overridenDefaultNamespace = defaultNamespace;

      // for now support just one JBossXmlNsPrefix
      String overrideNamespace = null;
      String prefixNs = null;
      JBossXmlNsPrefix xmlNsPrefix = property.getUnderlyingAnnotation(JBossXmlNsPrefix.class);
      if (xmlNsPrefix != null)
      {
         prefixNs = schemaBinding.getNamespace(xmlNsPrefix.prefix());
         if (prefixNs == null)
         {
            if (xmlNsPrefix.schemaTargetIfNotMapped())
               prefixNs = defaultNamespace;
            else
               throw new IllegalStateException("Prefix '" + xmlNsPrefix.prefix() + "' is not mapped to any namespace!");
         }
         
         if(xmlNsPrefix.applyToComponentQName())
            overrideNamespace = prefixNs;
      }

      JBossXmlGroup jbossXmlGroup = null;
      if (!propertyType.isPrimitive())
         jbossXmlGroup = ((ClassInfo) propertyType).getUnderlyingAnnotation(JBossXmlGroup.class);
      if(elements[0] == null && jbossXmlGroup != null)
      {
         if(prefixNs != null && xmlNsPrefix.applyToComponentContent())
            defaultNamespace = prefixNs;

         if (trace)
            log.trace("Processing group for property " + property.getName() + " in "
                  + property.getBeanInfo().getName() + " " + jbossXmlGroup);

         JBossXmlChild[] children = jbossXmlGroup.value();
         if (children != null && children.length > 0)
         {
            TypeBinding elementTypeBinding = new TypeBinding();
            elementTypeBinding.setSchemaBinding(schemaBinding);

            JBossXmlGroupText groupText = ((ClassInfo) propertyType).getUnderlyingAnnotation(JBossXmlGroupText.class);
            if (groupText != null && groupText.wrapper() != Object.class)
            {
               BeanInfo wrapperInfo = JBossXBBuilder.configuration.getBeanInfo(groupText.wrapper(), beanAccessMode);
               TypeBinding wrapperTypeBinding = resolveTypeBinding(wrapperInfo.getClassInfo());

               ParticleHandler particleHandler = wrapperTypeBinding.getHandler();
               if (particleHandler instanceof BeanHandler == false)
                  throw new IllegalStateException("Cannot wrap " + wrapperInfo.getName() + " not a bean type " + particleHandler);
               BeanHandler beanHandler = (BeanHandler) particleHandler;
               WrapperBeanAdapterFactory wrapperFactory = new WrapperBeanAdapterFactory(beanHandler.getBeanAdapterFactory(), propertyType.getType());
               BeanHandler wrapperHandler = new BeanHandler(wrapperInfo.getName(), wrapperFactory, elementTypeBinding);
               elementTypeBinding.setHandler(wrapperHandler);

               // Steal the attributes
               Collection<AttributeBinding> otherAttributes = wrapperTypeBinding.getAttributes();
               if (otherAttributes != null)
               {
                  for (AttributeBinding other : otherAttributes)
                  {
                     elementTypeBinding.addAttribute(other);
                     wrapperHandler.getAttributesHandler().addAttribute(other);
                  }
               }
               elementTypeBinding.setSimpleType(wrapperTypeBinding.getSimpleType());
            }
            else
            {
               elementTypeBinding.setHandler(BuilderParticleHandler.parentGroup(localModel));
            }

            QName propertyQName = generateXmlName(property.getName(), elementForm, overrideNamespace, null);
            ElementBinding elementBinding = createElementBinding(propertyType, elementTypeBinding, propertyQName, false);

            AbstractPropertyHandler propertyHandler = new PropertyHandler(property, propertyType);
            if(elementSetParentHandler)
               elementBinding.setHandler(new SetParentOverrideHandler(elementTypeBinding.getHandler(), propertyHandler));
            beanAdapterFactory.addProperty(propertyQName, propertyHandler);

            // Bind it to the model
            ParticleBinding particle = new ParticleBinding(elementBinding, 0, 1, false);
            localModel.addParticle(particle);

            // Setup the child model
            ChoiceBinding childModel = new ChoiceBinding(schemaBinding);
            childModel.setHandler(BuilderParticleHandler.setParentDelegate(elementTypeBinding.getHandler()));
            ParticleBinding particleBinding = new ParticleBinding(childModel);
            particleBinding.setMinOccurs(0);
            particleBinding.setMaxOccurs(1);
            elementTypeBinding.setParticle(particleBinding);

            JBossXmlGroupWildcard groupWildcard = ((ClassInfo) propertyType).getUnderlyingAnnotation(JBossXmlGroupWildcard.class);
            if (groupWildcard != null)
            {
               ChildWildcardHandler groupWildcardHandler;
               if (groupWildcard.wrapper() != Object.class)
               {
                  BeanInfo wrapperInfo = JBossXBBuilder.configuration.getBeanInfo(groupWildcard.wrapper(), beanAccessMode);
                  groupWildcardHandler = new ChildWildcardHandler(property, wrapperInfo, groupWildcard.property());
               }
               else
                  groupWildcardHandler = new ChildWildcardHandler(property);

               WildcardBinding wildcard = new WildcardBinding(schemaBinding);
               if (groupWildcard.lax())
                  wildcard.setProcessContents((short) 3); // Lax
               else
                  wildcard.setProcessContents((short) 1); // Strict

               particleBinding = new ParticleBinding(wildcard);
               particleBinding.setMinOccurs(0);
               particleBinding.setMaxOccurs(1);
               childModel.addParticle(particleBinding);

               elementTypeBinding.getWildcard().setHandler(groupWildcardHandler);
            }
            
            DefaultElementInterceptor interceptor = new PropertyInterceptor(property, propertyType);
            for (JBossXmlChild child : children)
            {
               QName childName = generateXmlName(child.name(), elementForm, child.namespace(), child.name());
               TypeInfo childType = JBossXBBuilder.configuration.getTypeInfo(child.type());

               TypeBinding childTypeBinding = resolveTypeBinding(childType);
               ElementBinding childBinding = createElementBinding(childType, childTypeBinding, childName, false);

               // Bind it to the model
               particle = new ParticleBinding(childBinding, child.minOccurs(), child.maxOccurs(), child.unbounded());
               particle.setMinOccurs(0);
               childModel.addParticle(particle);

               if(childType.isMap())
                  bindMapProperty(property, (ClassInfo) childType, childName, (ModelGroupBinding) childTypeBinding.getParticle().getTerm());
                                 
               elementTypeBinding.pushInterceptor(childName, interceptor);
               if (trace)
                  log.trace("Added interceptor " + childName + " for type=" + property.getBeanInfo().getName()
                        + " property=" + property.getName() + " interceptor=" + interceptor + " " + childType.getName());
            }
         }

         defaultNamespace = overridenDefaultNamespace;
         return;
      }

      XBValueAdapter valueAdapter = null;
      XmlJavaTypeAdapter xmlTypeAdapter = property.getUnderlyingAnnotation(XmlJavaTypeAdapter.class);
      if (xmlTypeAdapter != null)
         valueAdapter = new XBValueAdapter(xmlTypeAdapter.value(), propertyType.getTypeInfoFactory());

      JBossXmlPreserveWhitespace preserveSpace = property.getUnderlyingAnnotation(JBossXmlPreserveWhitespace.class);

      for (int i = 0; i < elements.length; ++i)
      {
         XmlElement element = elements[i];
         if (trace)
            log.trace("Processing " + element + " for type=" + property.getBeanInfo().getName() + " property=" + property.getName());

         // Determine the parameters
         String overrideName = null;
         boolean nillable = false;
         boolean required = false;

         TypeInfo localPropertyType = propertyType;

         if (element != null)
         {
            if(prefixNs == null || !xmlNsPrefix.applyToComponentQName())
               overrideNamespace = element.namespace();
            overrideName = element.name();
            nillable = element.nillable();
            required = element.required();
            Class<?> elementType = element.type();
            if (elementType != XmlElement.DEFAULT.class)
               localPropertyType = propertyType.getTypeInfoFactory().getTypeInfo(elementType);
         }

         // Determine the name
         QName propertyQName = generateXmlName(property.getName(), elementForm, overrideNamespace, overrideName);

         if(prefixNs != null && xmlNsPrefix.applyToComponentContent())
            defaultNamespace = prefixNs;

         AbstractPropertyHandler propertyHandler = null;

         // Create the element
         RepeatableParticleHandler repeatableHandler = null;
         if (valueAdapter != null)
         {
            localPropertyType = valueAdapter.getAdaptedTypeInfo();
            if(localPropertyType.isCollection())
               repeatableHandler = new CollectionRepeatableParticleHandler(new PropertyHandler(property, localPropertyType), (ClassInfo) localPropertyType, valueAdapter);
         }

         ModelGroupBinding targetGroup = localModel;
         boolean isCol = false;
         boolean isMap = false;

         TypeInfo colType = null;
         // a collection may be bound as a value of a complex type
         // and this is checked with the XmlType annotation
         if (propertyType.isCollection() && ((ClassInfo) propertyType).getUnderlyingAnnotation(XmlType.class) == null)
         {
            if(propertyHandler == null && !currentRepeatableHandlers)
               propertyHandler = new CollectionPropertyHandler(property, propertyType);
            
            isCol = true;
            colType = propertyType;
            // here we get the comp type based on the non-overriden property type...
            // which feels like a weak point
            TypeInfo typeArg = ((ClassInfo)property.getType()).getComponentType();
            if (typeArg != null)
            {
               JBossXmlChild xmlChild = ((ClassInfo) propertyType).getUnderlyingAnnotation(JBossXmlChild.class);
               if (xmlChild == null && localPropertyType.equals(propertyType))
               {  // the localPropertyType was not overridden previously so use the collection parameter type
                  localPropertyType = typeArg;
               }
            }
         }
         // TODO this shouldn't be here (because localPropertyType should specify an item?)
         // this is to support the Descriptions.class -> DescriptionsImpl.class
         else if (localPropertyType.isCollection()
               && ((ClassInfo) localPropertyType).getUnderlyingAnnotation(XmlType.class) == null)
         {
            if(propertyHandler == null && !currentRepeatableHandlers)
            {
               if (valueAdapter != null)
                  propertyHandler = new PropertyHandler(property, localPropertyType);
               else
                  propertyHandler = new CollectionPropertyHandler(property, localPropertyType);
            }

            isCol = true;
            colType = localPropertyType;
            localPropertyType = ((ClassInfo)localPropertyType).getComponentType();
         }
         else if (localPropertyType.isMap())
         {
            ElementBinding wrapperElement = null;
            if(elements.length > 1)
            {
               TypeBinding wrapperType = resolveTypeBinding(localPropertyType);
               wrapperElement = createElementBinding(localPropertyType, wrapperType, propertyQName, false);
               wrapperElement.setNillable(nillable);
               wrapperElement.setValueAdapter(valueAdapter);
               // Bind it to the model
               ParticleBinding particle = new ParticleBinding(wrapperElement, 0, 1, false);
               if (required == false)
                  particle.setMinOccurs(0);
               targetGroup.addParticle(particle);
               targetGroup = (ModelGroupBinding) wrapperType.getParticle().getTerm();
            }

            TermBinding entryTerm = bindMapProperty(property, (ClassInfo) localPropertyType, propertyQName, targetGroup);
            if(entryTerm != null)
            {
               QName entryQName = entryTerm.getQName();
               if(entryQName == null)
                  entryQName = propertyQName;
               if(wrapperElement != null)
               {
                  BeanAdapterFactory wrapperBeanFactory = ((BeanHandler)wrapperElement.getType().getHandler()).getBeanAdapterFactory();
                  Map<QName, AbstractPropertyHandler> properties = wrapperBeanFactory.getProperties();
                  if(properties.containsKey(entryQName) == false)
                  {
                     MapPropertyHandler mapHandler = new MapPropertyHandler(JBossXBBuilder.configuration, beanAccessMode, property, localPropertyType, true);
                     wrapperBeanFactory.addProperty(entryQName, mapHandler);
                  }
                  if(propertyHandler == null)
                     propertyHandler = new PropertyHandler(property, localPropertyType);
                  if(elementSetParentHandler)
                     wrapperElement.setHandler(new SetParentOverrideHandler(wrapperElement.getHandler(), propertyHandler));
               }
               else
               {
                  propertyQName = entryQName;
                  if(propertyHandler == null)
                     propertyHandler = new MapPropertyHandler(JBossXBBuilder.configuration, beanAccessMode, property, localPropertyType, false);
               }
               // overriding setParent doesn't make sense for a map
               // entryTerm.setHandler(new SetParentOverrideHandler(entryTerm.getHandler(), propertyHandler));
               isMap = true;
            }
         }

         if(propertyHandler == null)
            propertyHandler = new PropertyHandler(property, localPropertyType);
         
         ElementBinding elementBinding = null;
         ParticleBinding particle;
         if(Element.class.getName().equals(propertyType.getName()))
         {
            if(!wildcardProperty)
            {
               WildcardBinding wildcard = new WildcardBinding(schemaBinding);
               wildcard.setProcessContents((short) 2);
               wildcard.setUnresolvedElementHandler(DOMHandler.INSTANCE);
               wildcard.setUnresolvedCharactersHandler(DOMHandler.INSTANCE);

               SequenceBinding seq = new SequenceBinding(schemaBinding);
               seq.addParticle(new ParticleBinding(wildcard, 0, 1, false));

               TypeBinding elementTypeBinding = new TypeBinding();
               elementTypeBinding.setHandler(new NonXmlAnyElementDOMElementPropertyHandler(property, propertyType));
               elementTypeBinding.setParticle(new ParticleBinding(seq, 0, 1, true));

               elementBinding = createElementBinding(localPropertyType, elementTypeBinding, propertyQName, false);
               elementBinding.setNillable(nillable);
               elementBinding.setValueAdapter(valueAdapter);

               // Bind it to the model
               particle = new ParticleBinding(elementBinding, 1, 1, isCol);
               if (required == false)
                  particle.setMinOccurs(0);

               targetGroup.addParticle(particle);
            }
         }
         else if (!isMap)
         {
            TypeBinding elementType = resolveTypeBinding(localPropertyType);

            if (propertyXmlModelGroup != null)
               bindModelGroup(propertyXmlModelGroup, property, null, null, elementType, (ModelGroupBinding) elementType.getParticle().getTerm());

            elementBinding = createElementBinding(localPropertyType, elementType, propertyQName, false);
            elementBinding.setNillable(nillable);
            elementBinding.setValueAdapter(valueAdapter);

            if(repeatableHandler == null && elements.length == 1 && xmlWrapper == null)
            {
               if(propertyType.isArray())
               {
                  isCol = true;
                  if(currentRepeatableHandlers)
                     repeatableHandler = new ArrayWrapperRepeatableParticleHandler(propertyHandler);
                  else
                     repeatableHandler = new AppendingArrayRepeatableHandler(propertyHandler);
               }
               else if(isCol && currentRepeatableHandlers)
                  repeatableHandler = new CollectionRepeatableParticleHandler(propertyHandler, (ClassInfo) colType, null);
            }
            
            if(repeatableHandler != null)
               elementBinding.setRepeatableHandler(repeatableHandler);

            if (preserveSpace != null)
            {
               elementBinding.setNormalizeSpace(preserveSpace.preserve() ? false : true);
               if (trace)
                  log.trace("@JBossXmlPreserveWhitespace.preserve=" + preserveSpace.preserve() + " for " + elementBinding.getQName());
            }

            // Bind it to the model
            particle = new ParticleBinding(elementBinding, 1, 1, isCol);
            if (required == false)
               particle.setMinOccurs(0);

            targetGroup.addParticle(particle);
         }

         if(elementBinding != null && elementSetParentHandler)
            elementBinding.setHandler(new SetParentOverrideHandler(elementBinding.getType().getHandler(), propertyHandler));

         beanAdapterFactory.addProperty(propertyQName, propertyHandler);

         if (trace)
            log.trace("Added property " + propertyQName + " for type=" + property.getBeanInfo().getName() + " property="
                  + property.getName() + " handler=" + propertyHandler);

         defaultNamespace = overridenDefaultNamespace;
      }
   }

   private void bindModelGroup(JBossXmlModelGroup annotation, PropertyInfo property,
         BeanAdapterFactory beanAdapterFactory, AbstractPropertyHandler propertyHandler,
         TypeBinding typeBinding, ModelGroupBinding parentGroup)
   {
      String groupNs = defaultNamespace;
      String overridenDefaultNamespace = defaultNamespace;
      JBossXmlNsPrefix nsPrefix = property.getUnderlyingAnnotation(JBossXmlNsPrefix.class);
      if (nsPrefix != null)
      {
         String ns = schemaBinding.getNamespace(nsPrefix.prefix());
         if (ns == null && nsPrefix.schemaTargetIfNotMapped())
            throw new IllegalStateException("Prefix '" + nsPrefix.prefix() + "' is not mapped to any namespace!");

         if (nsPrefix.applyToComponentQName())
            groupNs = ns;
         if (nsPrefix.applyToComponentContent())
            defaultNamespace = ns;
      }

      QName groupName = null;
      if (!JBossXmlConstants.DEFAULT.equals(annotation.name()))
         groupName = new QName(groupNs, annotation.name());

      ModelGroupBinding group = null;
      boolean createGroup = true;
      if (groupName != null)
      {
         group = schemaBinding.getGroup(groupName);
         if(group != null)
            createGroup = false;
      }

      TypeInfo groupType = property.getType();
      boolean repeatable = false;
      if(groupType.isCollection() || groupType.isArray())
      {
         groupType = ((ClassInfo)groupType).getComponentType();
         repeatable = true;
      }

      if(createGroup)
      {
         boolean propOrderMissing = annotation.propOrder().length == 1 && annotation.propOrder()[0].equals("") || annotation.particles().length > 0;
         
         if(annotation.particles().length == 0)
         {
            BeanAccessMode beanAccessMode = this.beanAccessMode;
            JBossXmlAccessMode accessMode = annotation.accessMode();
            if(accessMode != JBossXmlAccessMode.NOT_SPECIFIED)
               beanAccessMode = jbossXmlAccessModeToBeanAccessMode(accessMode);
            
            // handler for the model group members
            BeanInfo groupBeanInfo = JBossXBBuilder.configuration.getBeanInfo(groupType, beanAccessMode);
            BeanAdapterFactory propBeanAdapterFactory = createAdapterFactory(DefaultBeanAdapterBuilder.class, groupBeanInfo, null);

            String[] memberOrder = annotation.propOrder();
            if (memberOrder.length == 0 || memberOrder[0].length() == 0)
            {
               List<String> propNames = new ArrayList<String>();
               for (PropertyInfo prop : groupBeanInfo.getProperties())
               {
                  if ("class".equals(prop.getName()))
                     continue;
                  propNames.add(prop.getName());
               }
               memberOrder = propNames.toArray(new String[propNames.size()]);
            }

            if (trace)
               log.trace("Property order for " + annotation.kind() + " property " + property.getName() + ": " + Arrays.asList(memberOrder));

            group = createModelGroup(annotation.kind(), groupType, memberOrder.length > 1 && propOrderMissing, annotation.propOrder(), groupName);
            group.setSkip(false);
            GroupBeanHandler propHandler = new GroupBeanHandler(groupBeanInfo.getName(), propBeanAdapterFactory, group);
            group.setHandler(propHandler);
            // can't do it with global components
            //group.setHandler(new SetParentOverrideHandler(propHandler, propertyHandler));

            boolean previousRepeatableHandlers = this.currentRepeatableHandlers;
            if(annotation.kind().equals(JBossXmlConstants.MODEL_GROUP_UNORDERED_SEQUENCE))
               this.currentRepeatableHandlers = false;
            else
               this.currentRepeatableHandlers = this.repeatableParticleHandlers;
            // bind model group members
            for (String memberPropName : memberOrder)
            {
               PropertyInfo memberProp = groupBeanInfo.getProperty(memberPropName);
               push(groupType, memberPropName);
               bindProperty(memberProp, group, propBeanAdapterFactory, memberOrder, false);
               pop();
            }
            this.currentRepeatableHandlers = previousRepeatableHandlers;
         }
         else
            group = createModelGroup(annotation.kind(), groupType, propOrderMissing, annotation.propOrder(), groupName);

         if(property.getType().isArray())
         {
            if(currentRepeatableHandlers)
               group.setRepeatableHandler(new ArrayWrapperRepeatableParticleHandler(propertyHandler));
            else
               group.setRepeatableHandler(new AppendingArrayRepeatableHandler(propertyHandler));
         }
      }
      
      parentGroup.addParticle(new ParticleBinding(group, 0, 1, repeatable));

      if(annotation.particles().length == 0)
      {
         if(group.getQName() == null)
            throw new JBossXBRuntimeException("To be bound a group must have a non-null QName. Bean " + property.getBeanInfo().getName() + ", property=" + property.getName());
         beanAdapterFactory.addProperty(group.getQName(), propertyHandler);
      }
      else
      {
         for (JBossXmlModelGroup.Particle member : annotation.particles())
         {
            XmlElement element = member.element();
            QName memberQName = generateXmlName(element.name(), XmlNsForm.QUALIFIED, element.namespace(), null);

            if(createGroup)
            {
               TypeInfo memberTypeInfo = groupType.getTypeInfoFactory().getTypeInfo(member.type());

               boolean isCol = false;
               if (memberTypeInfo.isCollection())
               {
                  memberTypeInfo = ((ClassInfo) memberTypeInfo).getComponentType();
                  isCol = true;
               }

               TypeBinding memberTypeBinding = resolveTypeBinding(memberTypeInfo);
               ElementBinding memberElement = createElementBinding(memberTypeInfo, memberTypeBinding, memberQName, false);
               memberElement.setNillable(true);
               ParticleBinding memberParticle = new ParticleBinding(memberElement, 0, 1, isCol);
               group.addParticle(memberParticle);
               // can't do it with global components (the group one)
               //if(propertyHandler != null)
               //   memberElement.setHandler(new SetParentOverrideHandler(memberElement.getHandler(), propertyHandler));
            }
            
            if(propertyHandler != null)
               beanAdapterFactory.addProperty(memberQName, propertyHandler);
            else
               typeBinding.pushInterceptor(memberQName, ChildCollectionInterceptor.SINGLETON);
         }
      }

      defaultNamespace = overridenDefaultNamespace;
   }

   private ModelGroupBinding createModelGroup(String kind, TypeInfo type, boolean propOrderMissing, String[] propertyOrder, QName groupName)
   {
      ModelGroupBinding group;
      if (kind.equals(JBossXmlConstants.MODEL_GROUP_SEQUENCE))
      {
         if(propOrderMissing)
            assertPropOrderNotRequired(type, propertyOrder);
         group = new SequenceBinding(schemaBinding);//groupFactory.createSequence(schemaBinding);
      }
      else if (kind.equals(JBossXmlConstants.MODEL_GROUP_UNORDERED_SEQUENCE))
         group = new UnorderedSequenceBinding(schemaBinding);
      else if (kind.equals(JBossXmlConstants.MODEL_GROUP_CHOICE))
         group = new ChoiceBinding(schemaBinding);
      else if (kind.equals(JBossXmlConstants.MODEL_GROUP_ALL))
         group = new AllBinding(schemaBinding);
      else if(kind.equals(JBossXmlConstants.DEFAULT))
      {
         if(propOrderMissing)
            assertPropOrderNotRequired(type, propertyOrder);
         group = groupFactory.createSequence(schemaBinding);
      }
      else
         throw new IllegalStateException("Unexpected JBossXmlModelGroup.kind=" + kind + " for type " + type.getName());
      
      if(groupName != null)
      {
         group.setQName(groupName);
         schemaBinding.addGroup(groupName, group);
      }
      return group;
   }
      
   private SequenceBinding bindXmlElementWrapper(AbstractPropertyHandler setParentProperty, TypeInfo propertyType, ModelGroupBinding parentModel, XmlElementWrapper annotation, QName wrapperQName)
   {
      TypeBinding wrapperType = new TypeBinding();
      SequenceBinding seq = new SequenceBinding(schemaBinding);
      seq.setHandler(DefaultHandlers.NOOP_PARTICLE_HANDLER);
      ParticleBinding particle = new ParticleBinding(seq);
      wrapperType.setParticle(particle);
      wrapperType.setHandler(DefaultHandlers.NOOP_PARTICLE_HANDLER);

      ElementBinding wrapperElement = createElementBinding(propertyType, wrapperType, wrapperQName, false);
      wrapperElement.setNillable(annotation.nillable());
      wrapperElement.setSkip(true);
      particle = new ParticleBinding(wrapperElement, annotation.required() ? 1 : 0, 1, propertyType.isCollection() || propertyType.isArray());
      parentModel.addParticle(particle);

      if (propertyType.isArray())
         if(currentRepeatableHandlers)
            wrapperElement.setRepeatableHandler(new ArrayWrapperRepeatableParticleHandler(setParentProperty));
         else
            wrapperElement.setRepeatableHandler(new AppendingArrayRepeatableHandler(setParentProperty));
      else if (propertyType.isCollection() && currentRepeatableHandlers)
         wrapperElement.setRepeatableHandler(new CollectionRepeatableParticleHandler(setParentProperty, (ClassInfo) propertyType, null));
      
      return seq;
   }

   private BeanAdapterFactory createAdapterFactory(Class<? extends BeanAdapterBuilder> beanAdapterBuilderClass, BeanInfo beanInfo, MethodInfo factory)
   {
      try
      {
         BeanInfo adapterBuilderInfo = JBossXBBuilder.configuration.getBeanInfo(beanAdapterBuilderClass, beanAccessMode);
         BeanAdapterBuilder adapterBuilder = (BeanAdapterBuilder) adapterBuilderInfo.newInstance();
         return adapterBuilder.newFactory(beanInfo, factory);
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Error creating BeanAdapterFactory for "
               + beanAdapterBuilderClass.getName(), t);
      }
   }

   /**
    * Add a namespace to the schema
    * 
    * @param namespace the namespace
    * @param erase whether to erase if there was only the default namespace
    */
   private void addNamespace(String namespace, boolean erase)
   {
      Set<String> namespaces = schemaBinding.getNamespaces();
      if (erase && namespaces.size() <= 1)
         namespaces = new HashSet<String>(Collections.singleton(namespace));
      namespaces.add(namespace);
      schemaBinding.setNamespaces(namespaces);
   }

   /**
    * Create a new xml name
    * 
    * @param typeInfo the type info
    * @param form the namespace form
    * @param namespace the override namespace
    * @param name the override name
    * @return the xml name
    */
   protected QName generateXmlName(TypeInfo typeInfo, XmlNsForm form, String namespace, String name)
   {
      return generateXmlName(typeInfo.getSimpleName(), form, namespace, name);
   }

   /**
    * Create a new xml name
    * 
    * @param localName the raw local name
    * @param form the namespace form
    * @param namespace the override namespace
    * @param name the override name
    * @return the xml name
    */
   protected QName generateXmlName(String localName, XmlNsForm form, String namespace, String name)
   {
      String nsUri = XMLConstants.NULL_NS_URI;
      if (form == XmlNsForm.QUALIFIED)
         nsUri = defaultNamespace;
      if (namespace != null && JBossXmlConstants.DEFAULT.equals(namespace) == false)
         nsUri = namespace;
      if (name != null && JBossXmlConstants.DEFAULT.equals(name) == false)
         localName = name;
      else
         localName = JBossXBBuilder.generateXMLNameFromJavaName(localName, true, schemaBinding.isIgnoreLowLine());
      return new QName(nsUri, localName);
   }

   private void push(TypeInfo typeInfo)
   {
      push(typeInfo, null);
   }

   private void push(TypeInfo typeInfo, String joinpoint)
   {
      locations.push(new Location(typeInfo, joinpoint));
   }

   private void pop()
   {
      locations.pop();
   }

   private RuntimeException rethrowWithLocation(Throwable t)
   {
      StringBuilder message = new StringBuilder();
      message.append(t.getMessage());
      message.append("\n");
      while (locations.isEmpty() == false)
      {
         Location location = locations.pop();
         location.append(message);
         if (locations.isEmpty() == false)
            message.append('\n');
      }
      throw new JBossXBRuntimeException(message.toString(), t);
   }

   /** A location */
   private class Location
   {
      /** The type info */
      TypeInfo typeInfo;

      /** The join point */
      String joinpoint;

      Location(TypeInfo typeInfo, String joinpoint)
      {
         this.typeInfo = typeInfo;
         this.joinpoint = joinpoint;
      }

      public void append(StringBuilder builder)
      {
         builder.append("at ");
         builder.append(typeInfo.getName());
         if (joinpoint != null)
            builder.append('.').append(joinpoint);
      }
   }

   private static class XBValueAdapter implements ValueAdapter
   {
      private final XmlAdapter<Object, ?> xmlAdapter;

      private final TypeInfo adaptedTypeInfo;
      private final Type adaptedType;
      
      public XBValueAdapter(Class<? extends XmlAdapter> adapterImplClass, TypeInfoFactory factory)
      {
         try
         {
            this.xmlAdapter = adapterImplClass.newInstance();
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Failed to create an instance of " + adapterImplClass.getName(), e);
         }

         adaptedType = ((ParameterizedType)adapterImplClass.getGenericSuperclass()).getActualTypeArguments()[0];
         adaptedTypeInfo = factory.getTypeInfo(adaptedType);
      }

      public TypeInfo getAdaptedTypeInfo()
      {
         return adaptedTypeInfo;
      }

      public Type getAdaptedType()
      {
         return adaptedType;
      }

      public Object cast(Object o, Class<?> c)
      {
         try
         {
            return xmlAdapter.unmarshal(o);
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Failed to adapt value " + o + " to type " + c, e);
         }
      }
   }
   
   private TermBinding bindMapProperty(PropertyInfo prop, ClassInfo propType, QName propertyQName, ModelGroupBinding targetGroup)
   {
      JBossXmlMapEntry entryElement = null;
      if(prop != null)
         entryElement = prop.getUnderlyingAnnotation(JBossXmlMapEntry.class);
      if(entryElement == null)
         entryElement = propType.getUnderlyingAnnotation(JBossXmlMapEntry.class);

      JBossXmlMapKeyElement keyElement = null;
      if(prop != null)
         keyElement = prop.getUnderlyingAnnotation(JBossXmlMapKeyElement.class);
      if(keyElement == null)
         keyElement = propType.getUnderlyingAnnotation(JBossXmlMapKeyElement.class);
      
      JBossXmlMapKeyAttribute keyAttribute = null;
      if(prop != null)
         keyAttribute = prop.getUnderlyingAnnotation(JBossXmlMapKeyAttribute.class);
      if(keyAttribute == null)
         keyAttribute = propType.getUnderlyingAnnotation(JBossXmlMapKeyAttribute.class);
      
      TermBinding entryTerm = null;
      if(keyElement != null || keyAttribute != null)
      {
         // further assuming the map is bound

         JBossXmlMapValueElement valueElement = null;
         if(prop != null)
            valueElement = prop.getUnderlyingAnnotation(JBossXmlMapValueElement.class);
         if(valueElement == null)
            valueElement = propType.getUnderlyingAnnotation(JBossXmlMapValueElement.class);
         
         JBossXmlMapValueAttribute valueAttribute = null;
         if(prop != null)
            valueAttribute = prop.getUnderlyingAnnotation(JBossXmlMapValueAttribute.class);
         if(valueAttribute == null)
            valueAttribute = propType.getUnderlyingAnnotation(JBossXmlMapValueAttribute.class);

         TypeInfo keyType = propType.getKeyType();
         TypeInfo valueType = propType.getValueType();

         // entry handler
         BeanAdapterFactory entryAdapterFactory = null;
         BeanInfo entryInfo = JBossXBBuilder.configuration.getBeanInfo(DefaultMapEntry.class, beanAccessMode);
         entryAdapterFactory = createAdapterFactory(DefaultBeanAdapterBuilder.class, entryInfo, null);

         TypeBinding entryType = null;
         TypeInfo entryTypeInfo = null;

         // bind the entry element if present
         if(entryElement != null && !JBossXmlConstants.DEFAULT.equals(entryElement.name()))
         {
            String ns = entryElement.namespace();
            if(JBossXmlConstants.DEFAULT.equals(ns))
               ns = defaultNamespace;                  
            QName entryName = new QName(ns, entryElement.name());

            entryType = new TypeBinding();
            entryType.setSchemaBinding(schemaBinding);
            BeanHandler entryHandler = new BeanHandler(entryInfo.getName(), entryAdapterFactory, entryType);
            entryType.setHandler(entryHandler);

            entryTypeInfo = JBossXBBuilder.configuration.getTypeInfo(DefaultMapEntry.class);                     
            ElementBinding entryElementBinding = createElementBinding(entryTypeInfo, entryType, entryName, false);
            entryTerm = entryElementBinding;
            ParticleBinding entryParticle = new ParticleBinding(entryElementBinding, 0, -1, true);
            targetGroup.addParticle(entryParticle);
               
            propertyQName = entryName;
               
            if(keyAttribute != null)
            {
               TypeBinding attributeType = resolveTypeBinding(keyType);
               AttributeHandler attributeHandler = new PropertyHandler(entryInfo.getProperty("key"), keyType);
               QName attrQName = generateXmlName(keyType, attributeForm, keyAttribute.namespace(), keyAttribute.name());
               AttributeBinding keyBinding = new AttributeBinding(schemaBinding, attrQName, attributeType, attributeHandler);
               keyBinding.setRequired(true);
               entryType.addAttribute(keyBinding);
               entryHandler.getAttributesHandler().addAttribute(keyBinding);
            }

            if(valueAttribute != null)
            {
               TypeBinding attributeType = resolveTypeBinding(valueType);
               AttributeHandler attributeHandler = new PropertyHandler(entryInfo.getProperty("value"), valueType);
               QName attrQName = generateXmlName(valueType, attributeForm, valueAttribute.namespace(), valueAttribute.name());
               AttributeBinding valueBinding = new AttributeBinding(schemaBinding, attrQName, attributeType, attributeHandler);
               valueBinding.setRequired(true);
               entryType.addAttribute(valueBinding);
               entryHandler.getAttributesHandler().addAttribute(valueBinding);
            }
            else if(valueElement == null)
            {
               CharactersHandler charactersHandler = new ValueHandler(entryInfo.getProperty("value"), valueType);
               entryType.setSimpleType(charactersHandler);
            }
         }
         
         SequenceBinding keyValueSequence = null;
         if(keyElement != null)
         {
            keyValueSequence = new SequenceBinding(schemaBinding);                     
            if(entryType == null)
            {
               keyValueSequence.setSkip(false);
               keyValueSequence.setQName(propertyQName);
               GroupBeanHandler entryHandler = new GroupBeanHandler(entryInfo.getName(), entryAdapterFactory, keyValueSequence);
               schemaBinding.addGroup(keyValueSequence.getQName(), keyValueSequence);
               ParticleBinding keyValueParticle = new ParticleBinding(keyValueSequence, 0, -1, true);
               targetGroup.addParticle(keyValueParticle);
               keyValueSequence.setHandler(entryHandler);
               entryTerm = keyValueSequence;
            }
            else
            {
               ParticleBinding keyValueParticle = new ParticleBinding(keyValueSequence, 1, 1, false);
               entryType.setParticle(keyValueParticle);
            }
            
            // key element
            TypeBinding keyTypeBinding = resolveTypeBinding(keyType);                  
            String keyNs = keyElement.namespace();
            if(JBossXmlConstants.DEFAULT.equals(keyNs))
               keyNs = defaultNamespace;                  
            ElementBinding keyElementBinding = createElementBinding(keyType, keyTypeBinding, new QName(keyNs, keyElement.name()), false);
            ParticleBinding particle = new ParticleBinding(keyElementBinding, 1, 1, false);
            keyValueSequence.addParticle(particle);
            PropertyHandler keyHandler = new PropertyHandler(entryInfo.getProperty("key"), keyType);
            entryAdapterFactory.addProperty(keyElementBinding.getQName(), keyHandler);
            if(elementSetParentHandler)
               keyElementBinding.setHandler(new SetParentOverrideHandler(keyTypeBinding.getHandler(), keyHandler));
         }
         
         if(valueElement != null)
         {
            TypeBinding valueTypeBinding = resolveTypeBinding(valueType);                  
            String valueNs = valueElement.namespace();
            if(JBossXmlConstants.DEFAULT.equals(valueNs))
               valueNs = defaultNamespace;                  
            ElementBinding valueElementBinding = createElementBinding(valueType, valueTypeBinding, new QName(valueNs, valueElement.name()), false);
            ParticleBinding particle = new ParticleBinding(valueElementBinding, 1, 1, false);
            keyValueSequence.addParticle(particle);
            PropertyHandler valueHandler = new PropertyHandler(entryInfo.getProperty("value"), valueType);
            entryAdapterFactory.addProperty(valueElementBinding.getQName(), valueHandler);
            if(elementSetParentHandler)
               valueElementBinding.setHandler(new SetParentOverrideHandler(valueTypeBinding.getHandler(), valueHandler));
         }
      }
      else if(entryElement != null && !JBossXmlMapEntry.DEFAULT.class.equals(entryElement.type()))
      {
         if(!JBossXmlConstants.DEFAULT.equals(entryElement.name()))
         {
            String ns = entryElement.namespace();
            if(JBossXmlConstants.DEFAULT.equals(ns))
               ns = propertyQName.getNamespaceURI();
            propertyQName = new QName(ns, entryElement.name());
         }

         TypeInfo entryTypeInfo = JBossXBBuilder.configuration.getTypeInfo(entryElement.type());
         ElementBinding entryElementBinding = createElementBinding(entryTypeInfo, propertyQName.getLocalPart(), false);
         ParticleBinding entryParticle = new ParticleBinding(entryElementBinding, 0, -1, true);
         targetGroup.addParticle(entryParticle);
         entryTerm = entryElementBinding;
      }
      
      return entryTerm;
   }
   
   private static interface ModelGroupFactory
   {
      ModelGroupBinding createSequence(SchemaBinding schema);
   }
   
   private static class DefaultModelGroupFactory implements ModelGroupFactory
   {
      final static DefaultModelGroupFactory INSTANCE = new DefaultModelGroupFactory();
      
      public ModelGroupBinding createSequence(SchemaBinding schema)
      {
         return new SequenceBinding(schema);
      }
   }
   
   private static class UnorderedSequenceModelGroupFactory implements ModelGroupFactory
   {
      final static UnorderedSequenceModelGroupFactory INSTANCE = new UnorderedSequenceModelGroupFactory();
      
      public ModelGroupBinding createSequence(SchemaBinding schema)
      {
         return new UnorderedSequenceBinding(schema);
      }
   }
}
