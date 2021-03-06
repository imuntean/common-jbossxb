/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.xb.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.jboss.xb.binding.resolver.MutableSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.AllBinding;
import org.jboss.xb.binding.sunday.unmarshalling.AttributeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ChoiceBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ModelGroupBinding;
import org.jboss.xb.binding.sunday.unmarshalling.ParticleBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SequenceBinding;
import org.jboss.xb.binding.sunday.unmarshalling.TermBinding;
import org.jboss.xb.binding.sunday.unmarshalling.TypeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.UnorderedSequenceBinding;
import org.jboss.xb.binding.sunday.unmarshalling.WildcardBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.binding.Constants;

/**
 * This class is used to check consistency between SchemaBinding instances and their corresponding XSD schemas.
 * It can be configured to exclude (or include) validation of certain namespaces
 * (i.e. types, elements and model groups from certain namespaces).
 * By default, namespace "http://www.w3.org/2001/XMLSchema" is excluded.
 *
 * Also specific types identified by their QName can be excluded or included from validation.
 * Simple XSD types are validated only if present in SchemaBinding. The reason for that is
 * many simple types are bound/represented in Java model by java.lang.String
 * which when building SchemaBinding is bound to xsd:string.
 *
 * Current implementation does not ensures complete consistency but only the basics such as
 * existence of type definitions, element declarations, element ordering in groups and
 * possible occurences of particles.
 *
 * When an inconsistency is found handleError(String msg) is called. The default implementation
 * of which throws IllegalStateException. Subclasses can override this method to report errors
 * differently.
 *
 * Sometimes the error itself may not be informative enough to e.g. identify the location
 * of the inconsistency in the schema. In this case logging should be enabled.
 * Default logger is an instance of org.jboss.logging.Logger with category org.jboss.xb.util.SchemaBindingValidator.
 * All the messages are logged from method log(String msg) which subclasses can override.
 *
 * @author <a href="alex@jboss.com">Alexey Loubyansky</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public class DefaultSchemaBindingValidator extends AbstractSchemaBindingValidator
{
   public DefaultSchemaBindingValidator()
   {
      this(null);
   }

   public DefaultSchemaBindingValidator(SchemaBindingResolver resolver)
   {
      super(resolver);
      reset();
      excludeNs(Constants.NS_XML_SCHEMA);
   }

   protected void validate(XSModel xsSchema, SchemaBinding schemaBinding)
   {
      try
      {
         /* TODO groups are not properly bound
         XSNamedMap groups = xsSchema.getComponents(XSConstants.MODEL_GROUP_DEFINITION);
         for(int i = 0; i < groups.getLength(); ++i)
         {
            XSModelGroupDefinition xsGroupDef = (XSModelGroupDefinition)groups.item(i);
            System.out.println(xsGroupDef.getName());
            QName groupQName = new QName(xsGroupDef.getNamespace(), xsGroupDef.getName());
            ModelGroupBinding groupBinding = schemaBinding.getGroup(groupQName);
            assertNotNull("Group " + groupQName + " exists in the schema binding.", groupBinding);
         }
         */

         XSNamedMap types = xsSchema.getComponents(XSConstants.TYPE_DEFINITION);
         for (int i = 0; i < types.getLength(); ++i)
         {
            XSTypeDefinition xsType = (XSTypeDefinition) types.item(i);
            if (excludedNs.contains(xsType.getNamespace()))
               continue;

            QName typeQName = new QName(xsType.getNamespace(), xsType.getName());
            if (excludedTypes.contains(typeQName))
               continue;

            TypeBinding typeBinding = schemaBinding.getType(typeQName);
            if (typeBinding == null)
            {
               SchemaBindingResolver resolver = getSchemaResolver();
               if(resolver != null)
               {
                  SchemaBinding foreignSchema = resolver.resolve(typeQName.getNamespaceURI(), null, null);
                  if(foreignSchema != null && foreignSchema != schemaBinding)
                     typeBinding = foreignSchema.getType(typeQName);
               }
            }
            
            if (typeBinding == null)
            {
               boolean ignoreIfNotFound = false;
               if (xsType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE)
               {
                  ignoreIfNotFound = true;
               }
               else
               {
                  XSComplexTypeDefinition xsComplexType = (XSComplexTypeDefinition) xsType;
                  if (xsComplexType.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE)
                  {
                     XSObjectList attributeUses = xsComplexType.getAttributeUses();
                     if (attributeUses.getLength() == 0)
                     {
                        ignoreIfNotFound = true;
                     }
                     else if (attributeUses.getLength() == 1)
                     {
                        XSAttributeUse xsAttrUse = (XSAttributeUse) attributeUses.item(0);
                        XSAttributeDeclaration xsAttr = xsAttrUse.getAttrDeclaration();
                        if (xsAttr.getNamespace() == null && "id".equals(xsAttr.getName()))
                           ignoreIfNotFound = true;
                     }
                  }
               }

               if (!ignoreIfNotFound)
               {
                  if (isLoggingEnabled())
                  {
                     log("SchemaBinding global types: ");
                     for (Iterator<TypeBinding> iter = schemaBinding.getTypes(); iter.hasNext();)
                     {
                        TypeBinding type = iter.next();
                        if (!excludedNs.contains(type.getQName().getNamespaceURI()))
                           log("- " + type.getQName());
                     }
                  }

                  handleError("TypeBinding " + typeQName + " is not found in the SchemaBinding.");
               }
            }
            else
            {
               validate(xsType, typeBinding);
            }
         }

         XSNamedMap elements = xsSchema.getComponents(XSConstants.ELEMENT_DECLARATION);
         for (int i = 0; i < elements.getLength(); ++i)
         {
            XSElementDeclaration xsElement = (XSElementDeclaration) elements.item(i);
            if (excludedNs.contains(xsElement.getNamespace()))
               continue;
            QName elementQName = new QName(xsElement.getNamespace(), xsElement.getName());
            ElementBinding elementBinding = schemaBinding.getElement(elementQName);
            
            if (elementBinding == null)
            {
               SchemaBindingResolver resolver = getSchemaResolver();
               if (resolver != null)
               {
                  SchemaBinding foreignSchema = resolver.resolve(elementQName.getNamespaceURI(), null, null);
                  if (foreignSchema != null && foreignSchema != schemaBinding)
                     elementBinding = foreignSchema.getElement(elementQName);
               }
            }
            
            if (elementBinding == null)
               handleError("ElementBinding " + elementQName + " is not found in the SchemaBinding.");
            validate(xsElement.getTypeDefinition(), elementBinding.getType());
         }
      }
      finally
      {
         reset();
      }
   }

   public void validate(XSElementDeclaration xsElement, ElementBinding elementBinding)
   {
      QName xsQName = new QName(xsElement.getNamespace(), xsElement.getName());
      if(!xsQName.equals(elementBinding.getQName()))
         handleError("Compared elements have different names: XSD QName is " + xsQName + ", ElementBinding QName is " + elementBinding.getQName());

      log("element " + xsQName);

      if(xsElement.getScope() == XSConstants.SCOPE_GLOBAL)
      {
         if(validatedElements.contains(xsQName))
            return;
         validatedElements.add(xsQName);
      }

      validate(xsElement.getTypeDefinition(), elementBinding.getType());
   }

   public void validate(XSTypeDefinition xsType, TypeBinding typeBinding)
   {
      if(xsType.getName() == null)
      {
         if(typeBinding.getQName() != null)
            handleError("XSD type is anonymous but TypeBinding has QName " + typeBinding.getQName());
      }
      else
      {
         if(excludedNs.contains(xsType.getNamespace()))
            return;

         QName xsQName = new QName(xsType.getNamespace(), xsType.getName());
         if(validatedTypes.contains(xsQName) || excludedTypes.contains(xsQName))
            return;

         validatedTypes.add(xsQName);
      }

      if(xsType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE)
         validate((XSSimpleTypeDefinition)xsType, typeBinding);
      else
         validate((XSComplexTypeDefinition)xsType, typeBinding);
   }

   public void validate(XSSimpleTypeDefinition xsType, TypeBinding typeBinding)
   {
      // TODO there could xsd types that are mapped to String which is bound by default to xsd:string
      //QName xsQName = xsType.getName() == null ? null : new QName(xsType.getNamespace(), xsType.getName());
      //assertEquals("Simple type expected to be " + (xsType == null ? "anonymous" : "named '" + xsQName + "'"), xsQName, typeBinding.getQName());

      if(!typeBinding.isSimple())
      {
         if(typeBinding.getValueAdapter() == null)
            handleError("XSD type " + typeBinding.getQName() + " is simple but TypeBinding is not and ValueAdapter for TypeBinding is not provided.");
      }
      // TODO the rest of the simple type stuff?
   }

   public void validate(XSComplexTypeDefinition xsType, TypeBinding typeBinding)
   {
      QName xsQName = xsType.getName() == null ? null : new QName(xsType.getNamespace(), xsType.getName());

      log("complex type " + xsQName);

      if(typeBinding.isSimple() &&
         typeBinding.getQName() != null &&
         Constants.NS_XML_SCHEMA.equals(typeBinding.getQName().getNamespaceURI()))
      {
         // perhaps the complex xsd type is equivalent to the built-in simple type
         if(xsType.getAttributeUses().getLength() == 0 &&
            xsType.getAttributeWildcard() == null &&
            xsType.getParticle() == null)
         {
            log(xsQName == null ? "Anonymous" : xsQName + " type is assumed to be equivalent to " + typeBinding.getQName());
            return;
         }
      }
      
      if(xsQName == null && typeBinding.getQName() != null ||
            xsQName != null && !xsQName.equals(typeBinding.getQName()))
         handleError("Compared complex types have different names: XSD QName is " + xsQName + ", TypeBindign QName is " + typeBinding.getQName());

      XSObjectList xsAttrUses = xsType.getAttributeUses();
      if(xsAttrUses.getLength() == 0)
      {
         // TODO missing id attributes in the schema
         //assertTrue("Type " + typeBinding.getQName() + " has no attributes in the schema", typeBinding.getAttributes().isEmpty());
      }
      else
      {
         for(int i = 0; i < xsAttrUses.getLength(); ++i)
         {
            XSAttributeDeclaration xsAttr = ((XSAttributeUse)xsAttrUses.item(i)).getAttrDeclaration();
            QName xsAttrQName = new QName(xsAttr.getNamespace(), xsAttr.getName());
            AttributeBinding attrBinding = typeBinding.getAttribute(xsAttrQName);
            if(attrBinding == null)
               handleError("Attribute " + xsAttrQName + " is not found in TypeBinding " + typeBinding.getQName());
            validate(xsAttr.getTypeDefinition(), attrBinding.getType());
         }
      }

      XSWildcard xsAttrWildcard = xsType.getAttributeWildcard();
      if(xsAttrWildcard != null && typeBinding.getAnyAttribute() == null)
         handleError("TypeBinding " + typeBinding.getQName() + " doesn't have AnyAttributeBinding");

      XSSimpleTypeDefinition xsSimpleType = xsType.getSimpleType();
      if(xsSimpleType != null)
      {
         TypeBinding simpleTypeBinding = typeBinding.getSimpleType();
         if(simpleTypeBinding == null)
            handleError("XSD type " + typeBinding.getQName() + " allows text content but its corresponding TypeBinding doesn't.");
         validate(xsSimpleType, simpleTypeBinding);
      }

      XSParticle xsParticle = xsType.getParticle();
      if(xsParticle != null)
      {
         ParticleBinding particleBinding = typeBinding.getParticle();
         if(particleBinding == null)
            handleError("TypeBinding " + xsQName + " doesn't contain a ParticleBinding.");
         validate(xsParticle, particleBinding);
      }
   }

   public void validate(XSParticle xsParticle, ParticleBinding particleBinding)
   {
      XSTerm xsTerm = xsParticle.getTerm();
      TermBinding termBinding = particleBinding.getTerm();
      if(termBinding == null)
         handleError("ParticleBinding doesn't contain a TermBinding.");
      short xsTermType = xsTerm.getType();
      String termStr = null;
      boolean maxOccursUnbounded = particleBinding.getMaxOccursUnbounded();
      if(xsTermType == XSConstants.MODEL_GROUP)
      {
         termStr = "sequence";
         XSModelGroup xsModelGroup = (XSModelGroup)xsTerm;
         short xsModelGroupCompositor = (xsModelGroup).getCompositor();
         if(XSModelGroup.COMPOSITOR_CHOICE == xsModelGroupCompositor)
            termStr = "choice";
         else if(XSModelGroup.COMPOSITOR_ALL == xsModelGroupCompositor)
            termStr = "all";

         if(!termBinding.isModelGroup())
         {
            // TODO review this
            // let's see whether it's wrapped
            if(xsModelGroup.getParticles().getLength() == 1)
            {
               XSParticle xsWrappedParticle = (XSParticle) xsModelGroup.getParticles().item(0);
               validate(xsWrappedParticle, particleBinding);
            }
            else
               handleError("TermBinding expected to be a " + termStr + " but was " + termBinding);
         }
         else
            validate(xsModelGroup, particleBinding);
      }
      else if(xsTermType == XSConstants.ELEMENT_DECLARATION)
      {
         XSElementDeclaration xsElement = (XSElementDeclaration) xsTerm;
         QName xsElementName = new QName(xsElement.getNamespace(), xsElement.getName());
         termStr = xsElementName.toString();

         if(!termBinding.isElement())
         {
            // TODO sometimes XB wraps (maybe unnecessarily) repeatable elements into a sequence.
            // the same xml structure can be described differently in xsd
            // There is a weird array binding structure in XB
            TermBinding t = termBinding;
            while (/*(xsParticle.getMaxOccursUnbounded() || xsParticle.getMaxOccurs() > 1) &&*/
                  t instanceof SequenceBinding)
            {
               SequenceBinding seq = (SequenceBinding) t;
               Collection<ParticleBinding> particles = seq.getParticles();
               if(particles.size() == 1)
               {
                  ParticleBinding particle = particles.iterator().next();
                  if(particle.getMaxOccursUnbounded())
                     maxOccursUnbounded = true;
                  t = particle.getTerm();
                  if(t.isElement())
                  {
                     particleBinding = particle;
                     termBinding = t;
                  }
               }
               else
                  break;
            }

            if(!termBinding.isElement())
               handleError("TermBinding expected to be element " + termStr + " but was " + termBinding);
         }

         validate(xsElement, (ElementBinding)termBinding);
      }
      else if(xsTermType == XSConstants.WILDCARD)
      {
         if(!termBinding.isWildcard())
            handleError("TermBinding expected to be a wildcard but was " + termBinding);
         XSWildcard xsWildcard = (XSWildcard) xsTerm;
         WildcardBinding wildcardBinding = (WildcardBinding) termBinding;
         if(xsWildcard.getProcessContents() != wildcardBinding.getProcessContents())
            throw new IllegalStateException("Wildcard processContents doesn't match: XSD processContents is " + xsWildcard.getProcessContents() +
                  ", WildcardBinding processContents is " + wildcardBinding.getProcessContents());
         termStr = "wildcard";
      }
      else
         handleError("Unexpected XSTerm type: " + xsTermType);

      // TODO minOccurs is not trivial for flattened choices
      //assertEquals("ParticleBinding<" + termStr + "> min occurs.", xsParticle.getMinOccurs(), particleBinding.getMinOccurs());

      if(xsParticle.getMaxOccursUnbounded())
      {
         if(!maxOccursUnbounded && !(termBinding instanceof UnorderedSequenceBinding))
            handleError("XSD particle of " + termStr + " has maxOccurs unbounded but ParticleBinding of " + particleBinding.getTerm() + " does not.");
      }
      else if(xsParticle.getMaxOccurs() != particleBinding.getMaxOccurs())
         handleError("maxOccurs for particle of " + particleBinding.getTerm() + " don't match: XSD maxOccurs=" + xsParticle.getMaxOccurs() +
               ", ParticleBinding maxOccurs=" + particleBinding.getMaxOccurs());
   }

   public void validate(XSModelGroup xsModelGroup, ParticleBinding groupParticle)
   {
      ModelGroupBinding modelGroupBinding = (ModelGroupBinding) groupParticle.getTerm();
      short xsCompositor = xsModelGroup.getCompositor();
      boolean all = false;
      if(xsCompositor == XSModelGroup.COMPOSITOR_SEQUENCE)
      {
         log("sequence");
         if(!(modelGroupBinding instanceof SequenceBinding))
         {
            // another chance...
            if(modelGroupBinding instanceof AllBinding || modelGroupBinding instanceof UnorderedSequenceBinding)
               all = true;
            else
            {
               // here we're going to try to detect an extended choice wrapped by xerces into a sequence
               boolean extendedChoice = false;
               if(modelGroupBinding instanceof ChoiceBinding)
               {
                  XSObjectList particles = xsModelGroup.getParticles();
                  // there should be two particles
                  if(particles.getLength() == 2)
                  {
                     XSParticle p = (XSParticle) particles.item(0);
                     XSTerm term = p.getTerm();
                     // first particle must be a choice
                     if(term.getType() == XSConstants.MODEL_GROUP)
                     {
                        if(((XSModelGroup)term).getCompositor() == XSModelGroup.COMPOSITOR_CHOICE)
                        {
                           p = (XSParticle) particles.item(1);
                           term = p.getTerm();
                           // second particle can be a choice or an empty sequence
                           if(term.getType() == XSConstants.MODEL_GROUP)
                           {
                              XSModelGroup extensionGroup = (XSModelGroup) term;
                              if(extensionGroup.getCompositor() == XSModelGroup.COMPOSITOR_CHOICE)
                                 extendedChoice = true;
                              else if(extensionGroup.getCompositor() == XSModelGroup.COMPOSITOR_SEQUENCE &&
                                    extensionGroup.getParticles().getLength() == 0)
                                 extendedChoice = true;
                           }
                        }
                     }
                  }
               }
               
               if(extendedChoice)
               {
                  log(" - the sequence is treated as an extended choice");
                  all = true;
               }
               else
                  handleError("ModelGroupBinding expected to be a sequence but was " + modelGroupBinding);
            }
         }
      }
      else if(xsCompositor == XSModelGroup.COMPOSITOR_CHOICE)
      {
         log("choice");
         if(modelGroupBinding instanceof SequenceBinding)
         {
            // another chance...
            Collection<ParticleBinding> particles = modelGroupBinding.getParticles();
            if(particles.size() == 1)
            {
               ParticleBinding particleBinding = particles.iterator().next();
               if(particleBinding.getTerm() instanceof ChoiceBinding)
                  modelGroupBinding = (ModelGroupBinding) particleBinding.getTerm();
            }
         }

         if(!(modelGroupBinding instanceof ChoiceBinding || modelGroupBinding instanceof UnorderedSequenceBinding))
            handleError("XSD model group is choice but ModelGroupBinding is " + modelGroupBinding);
         
         // ordering in the choice is not important
         all = true;
      }
      else if(xsCompositor == XSModelGroup.COMPOSITOR_ALL)
      {
         log("all");
         if(!(modelGroupBinding instanceof AllBinding))
            handleError("XSD model group is all but ModelGroupBinding is " + modelGroupBinding);
         all = true;
      }
      else
         handleError("Unexpected compositor type for model group " + xsCompositor);


      XSObjectList xsParticles = xsModelGroup.getParticles();
      Collection<ParticleBinding> particleBindings = modelGroupBinding.getParticles();
      Map<QName, XSParticle> xsElementParticles = null;
      Map<QName, ParticleBinding> elementParticles = null;
      if(xsParticles.getLength() > 0)
      {
         if(particleBindings == null)
            handleError("XSD model group has " + xsParticles.getLength() + " particles but ModelGroupBinding doesn't have any.");
         if(xsParticles.getLength() != particleBindings.size() || all)
         {
            // let's try making it flat... to the elements
            xsElementParticles = new HashMap<QName, XSParticle>();
            flatten(xsModelGroup, xsElementParticles);
            elementParticles = new HashMap<QName, ParticleBinding>();
            flatten(groupParticle, elementParticles);

            if(xsElementParticles.size() != elementParticles.size())
            {
               if (isLoggingEnabled())
               {
                  String msg = "expected particles:\n";
                  for (int i = 0; i < xsParticles.getLength(); ++i)
                  {
                     XSTerm xsTerm = ((XSParticle) xsParticles.item(i)).getTerm();
                     short type = xsTerm.getType();
                     if (type == XSConstants.MODEL_GROUP)
                     {
                        short compositor = ((XSModelGroup) xsTerm).getCompositor();
                        if (compositor == XSModelGroup.COMPOSITOR_SEQUENCE)
                           msg += "- sequence\n";
                        else if (compositor == XSModelGroup.COMPOSITOR_CHOICE)
                           msg += "- choice\n";
                        else if (compositor == XSModelGroup.COMPOSITOR_ALL)
                           msg += "- all\n";
                     }
                     else if (type == XSConstants.ELEMENT_DECLARATION)
                     {
                        XSElementDeclaration element = (XSElementDeclaration) xsTerm;
                        msg += "- " + new QName(element.getNamespace(), element.getName()) + "\n";
                     }
                     else
                     {
                        msg += "- wildcard\n";
                     }
                  }

                  msg += "actual particles:\n";
                  Iterator<ParticleBinding> iter = particleBindings.iterator();
                  while (iter.hasNext())
                  {
                     TermBinding term = iter.next().getTerm();
                     if (term.isModelGroup())
                     {
                        if (term instanceof SequenceBinding)
                           msg += "- sequence\n";
                        else if (term instanceof ChoiceBinding)
                           msg += "- choice\n";
                        else
                           msg += "- wildcard\n";
                     }
                     else if (term.isElement())
                        msg += "- " + ((ElementBinding) term).getQName() + "\n";
                     else
                        msg += "- wildcard";
                  }
                  log(msg);

                  List<QName> missing = new ArrayList<QName>(xsElementParticles.keySet());
                  missing.removeAll(elementParticles.keySet());
                  log("flattened ModelGroupBinding is missing: ");
                  for (Iterator<QName> missingNames = missing.iterator(); missingNames.hasNext();)
                     log("- " + missingNames.next());

                  missing = new ArrayList<QName>(elementParticles.keySet());
                  missing.removeAll(xsElementParticles.keySet());
                  log("flattened XSModelGroup is missing: ");
                  for (Iterator<QName> missingNames = missing.iterator(); missingNames.hasNext();)
                     log("- " + missingNames.next());
               }
               handleError("ModelGroupBinding expected to have " + xsParticles.getLength() + " particle(s) but has " + particleBindings.size());
            }
         }
      }

      if(xsElementParticles != null)
      {
         Iterator<ParticleBinding> iter = elementParticles.values().iterator();
         while(iter.hasNext())
         {
            ParticleBinding particleBinding = iter.next();
            QName particleQName;
            TermBinding termBinding = particleBinding.getTerm();
            if(termBinding.isWildcard())
               particleQName = WILDCARD;
            else
               particleQName = ((ElementBinding)termBinding).getQName();
            XSParticle xsParticle = xsElementParticles.get(particleQName);
            if(xsParticle == null)
            {
               if(particleQName == WILDCARD)
                  handleError("WildcardBinding is missing");
               else
                  handleError("ElementBinding " + particleQName + " is missing: " + xsElementParticles.keySet());
            }
            validate(xsParticle, particleBinding);
         }
      }
      else
      {
         Iterator<ParticleBinding> iter = particleBindings.iterator();
         for (int i = 0; i < xsParticles.getLength(); ++i)
         {
            XSParticle xsParticle = (XSParticle) xsParticles.item(i);
            validate(xsParticle, iter.next());
         }
      }
   }

   private void flatten(XSModelGroup xsModelGroup, Map<QName, XSParticle> elementParticles)
   {
      XSObjectList xsParticles = xsModelGroup.getParticles();
      for(int i = 0; i < xsParticles.getLength(); ++i)
      {
         XSParticle particle = (XSParticle)xsParticles.item(i);
         XSTerm term = particle.getTerm();
         short termType = term.getType();
         if(termType == XSConstants.ELEMENT_DECLARATION)
         {
            XSElementDeclaration element = (XSElementDeclaration) term;
            QName qName = new QName(element.getNamespace(), element.getName());
            elementParticles.put(qName, particle);
         }
         else if(termType == XSConstants.WILDCARD)
            elementParticles.put(WILDCARD, particle);
         else
         {
            XSModelGroup modelGroup = (XSModelGroup) term;
            flatten(modelGroup, elementParticles);
         }
      }
   }

   private void flatten(ParticleBinding groupParticle, Map<QName, ParticleBinding> elementParticles)
   {
      TermBinding groupTerm = groupParticle.getTerm();
      if(!(groupTerm instanceof ModelGroupBinding))
         throw new IllegalStateException("The term is expected to be a model group but was " + groupTerm);
      
      ModelGroupBinding group = (ModelGroupBinding) groupTerm;
      boolean forceUnbounded = groupParticle.getMaxOccursUnbounded() && group.getParticles().size() == 1;
      Iterator<ParticleBinding> i = group.getParticles().iterator();
      while(i.hasNext())
      {
         ParticleBinding particle = i.next();
         TermBinding term = particle.getTerm();
         if(forceUnbounded && !particle.getMaxOccursUnbounded())
            particle = new ParticleBinding(term, particle.getMinOccurs(), particle.getMaxOccurs(), true);
         if(term.isElement())
         {
            ElementBinding element = (ElementBinding) term;
            elementParticles.put(element.getQName(), particle);
         }
         else if(term.isWildcard())
            elementParticles.put(WILDCARD, particle);
         else
         {
            ModelGroupBinding modelGroup = (ModelGroupBinding) term;
            flatten(particle, elementParticles);
         }
      }
   }

   /**
    * This an error handler method. Default implementation throws IllegalStateException with the message passed in as the argument.
    *
    * @param msg  the error message
    */
   protected void handleError(String msg)
   {
      throw new IllegalStateException(msg);
   }

   /**
    * This method is supposed to log a message. Default implementation uses trace logging.
    *
    * @param msg  the message to log.
    */
   protected void log(String msg)
   {
      if(isLoggingEnabled())
         log.trace(msg);
   }
}