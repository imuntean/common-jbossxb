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
package org.jboss.test.xml.choice;

import java.util.Collection;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 37406 $</tt>
 */
public class Root
{
   private Collection<Choice1> choice1;
   private Collection<Choice2> choice2;

   public Collection<Choice1> getChoice1()
   {
      return choice1;
   }

   public void setChoice1(Collection<Choice1> choice1)
   {
      this.choice1 = choice1;
   }

   public Collection<Choice2> getChoice2()
   {
      return choice2;
   }

   public void setChoice2(Collection<Choice2> choice2)
   {
      this.choice2 = choice2;
   }

   public String toString()
   {
      return "[choice1=" + choice1 + ", choice2=" + choice2 + "]";
   }

   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }
      if(!(o instanceof Root))
      {
         return false;
      }

      final Root root = (Root)o;

      if(choice1 != null ? !choice1.equals(root.choice1) : root.choice1 != null)
      {
         return false;
      }
      if(choice2 != null ? !choice2.equals(root.choice2) : root.choice2 != null)
      {
         return false;
      }

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (choice1 != null ? choice1.hashCode() : 0);
      result = 29 * result + (choice2 != null ? choice2.hashCode() : 0);
      return result;
   }
}
