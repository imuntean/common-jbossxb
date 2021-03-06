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
package org.jboss.xb.binding.sunday.unmarshalling;

import java.util.Map;
import java.util.Properties;

/**
 * SchemaResolverConfigMBean.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision$
 */
public interface SchemaResolverConfigMBean
{
   /**
    * Get whether schemas are cached by namespaceURI
    * @return true if schemas are cached
    */
   public boolean getCacheResolvedSchemas();
   /**
    * Set whether schemas are cached by namespaceURI
    * @param flag - true if schemas are cached
    */
   public void setCacheResolvedSchemas(boolean flag);

   /**
    * Get the schemaInitializers.
    * 
    * @return the schemaInitializers.
    */
   Properties getSchemaInitializers();

   /**
    * Set the schemaInitializers.
    * 
    * @param schemaInitializers the schemaInitializers.
    */
   void setSchemaInitializers(Properties schemaInitializers);

   /**
    * Get the schemaInitializers.
    * 
    * @return the schemaInitializers.
    */
   Map<String, SchemaBindingInitializer> getSchemaInitializerInstances();

   /**
    * Set the schemaInitializers.
    * 
    * @param schemaInitializers the schemaInitializers.
    */
   void setSchemaInitializerInstances(Map<String, SchemaBindingInitializer> schemaInitializers);

   /**
    * Get the schemaLocations.
    * 
    * @return the schemaLocations.
    */
   Properties getSchemaLocations();

   /**
    * Set the schemaLocations.
    * 
    * @param schemaLocations the schemaLocations.
    */
   void setSchemaLocations(Properties schemaLocations);

   /**
    * Get the parseAnnotations.
    * 
    * @return the parseAnnotations.
    */
   Properties getParseAnnotations();

   /**
    * Set the parseAnnotations.
    * 
    * @param parseAnnotations the parseAnnotations.
    */
   void setParseAnnotations(Properties parseAnnotations);

   /**
    * Get the JBossXBBuilder namespace to Class mappings.
    * 
    * @return bindingClasses
    */
   public Properties getBindingClasses();

   /**
    * Set the JBossXBBuilder namespace to Class mappings.
    * 
    * @param bindingClasses
    */
   public void setBindingClasses(Properties bindingClasses);

   /**
    * Get the JBossXBBuilder schema location to Class mappings.
    * 
    * @return bindingClasses
    */
   public Properties getBindingClassesByLocations();

   /**
    * Set the JBossXBBuilder schema location to Class mappings.
    * 
    * @param bindingClasses
    */
   public void setBindingClassesByLocations(Properties bindingClasses);
}
