/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.security.report;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.osgi.util.NLS;


/**
 * special implementation of {@link ClassLoader} that prevents loading classes out of the sernet-namespace except some special utility classes.
 * This is loader is used by beanshell interpreter to execute/evaluate beanshell-code within BIRT-report-templates.
 * See {@link Query} for implementation of {@link Interpreter}
 * 
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class ReportClassLoader extends ClassLoader {
    
    
    private static final Logger LOG = Logger.getLogger(ReportClassLoader.class);
    
    private static final Set<String> AUTHORIZED_FOR_EXTERNAL_USE = new HashSet<>();
    
    private ClassLoader parentClassLoader;
    
    static{
        AUTHORIZED_FOR_EXTERNAL_USE.add("sernet.");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.lang.Integer");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.lang.String"); // Covers java.lang.StringBuilder also
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.lang.Double");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.lang.Object");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.Arrays");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.Collections");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.List");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.Set");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.HashSet");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.ArrayList");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.regex");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.Comparator");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.Map");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.Map$Entry");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.util.Date");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.text.DateFormat");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.text.SimpleDateFormat");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.math.BigDecimal");
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.math.RoundingMode");
        AUTHORIZED_FOR_EXTERNAL_USE.add("org.apache.commons.lang.ArrayUtils");
        
        
        AUTHORIZED_FOR_EXTERNAL_USE.add("java.lang.Thread");
        
    }
    
    public ReportClassLoader (ClassLoader parentClassloader){
        super();
        this.parentClassLoader = parentClassloader;
    }
    
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        name = guessByStandardMapping(name);
        if (isTrustedClass(name)){
            try{
                Class<?> trustedClass = parentClassLoader.loadClass(name);
                LOG.debug("authorized class:\t" + name + " loaded via parent classloader");
                return trustedClass;
            }catch (ClassNotFoundException e){
                return tryAlternativClassloadingApproach(name);
            } 
        } else if (!AUTHORIZED_FOR_EXTERNAL_USE.contains(name)){
            return tryLoadingClassByGuessingItsName(name);
        } 
        LOG.debug("[block3]can not determine class for name:\t" + name);
        throw getSecurityClassLoadingException(name);
    }


    /**
     * @param name
     * @return
     * @throws ClassNotFoundException
     * @throws ClassLoadingDeniedException
     */
    private Class<?> tryLoadingClassByGuessingItsName(String name) throws ClassNotFoundException, ClassLoadingDeniedException {
        String qualifiedName = tryGuessingQualifiedClassname(name);
        if (!name.equals(qualifiedName)){
            Class<?> trustedClass = parentClassLoader.loadClass(qualifiedName);
            LOG.debug("loading class:\t" + qualifiedName + " for input:\t" + name);
            return trustedClass;
        } else {
            LOG.debug("[block2]can not determine class for name:\t" + name);
            throw getSecurityClassLoadingException(name);   
        }
    }


    /**
     * @param name
     * @throws ClassNotFoundException
     * @throws ClassLoadingDeniedException
     */
    private Class tryAlternativClassloadingApproach(String name) throws ClassNotFoundException, ClassLoadingDeniedException {
        LOG.debug("Class " + name + " not found, trying next way");
        if (!AUTHORIZED_FOR_EXTERNAL_USE.contains(name)){
            String qualifiedName = tryGuessingQualifiedClassname(name);
            if (!name.equals(qualifiedName)){
                Class<?> trustedClass =  parentClassLoader.loadClass(qualifiedName);
                LOG.debug("class:\t" + qualifiedName + " for input:\t" + name + " successfully loaded by parentclassloader");
                return trustedClass;
            } else {
                throw getSecurityClassLoadingException(name);   
            }
        } else {
            LOG.debug("[block1]can not determine class for name:\t" + name);
        }
        throw new ClassNotFoundException("Could not determine path of class:\t" + name + " within classpath");
    }


    private ClassLoadingDeniedException getSecurityClassLoadingException(String name) {
        ClassLoadingDeniedException e = new ClassLoadingDeniedException(NLS.bind(Messages.CLASSLOADING_DENIED_EXCEPTION_0, name));
        if(LOG.isDebugEnabled()){
            LOG.error("Could not load class due to verinice security policies", e);
        }
        return e;
    }
    
    private String tryGuessingQualifiedClassname(String name){
        String mappedName = guessByStandardMapping(name);
        if (!mappedName.equals(name)){
            return mappedName;
        }
        for (String qualifiedName : AUTHORIZED_FOR_EXTERNAL_USE){
            if (qualifiedName.contains(name)){
                return qualifiedName;
            }
        }

        for (String qualifiedName : AUTHORIZED_FOR_EXTERNAL_USE){
            if (qualifiedName.endsWith(getPathFreeClassName(name))){
                return qualifiedName;
            }
        }
        
        return name;
    }


    /**
     * @param name
     */
    private String guessByStandardMapping(String name) {
        if("Entry".equals(name)){
            return "java.util.Map$Entry";
        } else if ("Date".equals(name)){
            return "java.util.Date";
        } else if ("Object".equals(name)){
            return "java.lang.Object";
        } else if ("String".equals(name)){
            return "java.lang.String";
        } else if ("List".equals(name)){
            return "java.util.List";
        } else if ("Collections".equals(name)){
            return "java.util.Collections";
        } else if ("Comparator".equals(name)){
            return "java.util.Comparator";
        } else if ("Integer".equals(name)){
            return "java.lang.Integer";
        } else return name;
    }


    private boolean isTrustedClass(String name){
        if (name != null){
            return checkPrefixes(name);
        }
        return false;
    }

    private boolean checkPrefixes(String name) {
        for (String prefix : AUTHORIZED_FOR_EXTERNAL_USE){
            if (name.startsWith(prefix)){
                return true;
            }
        }
        return false;
    }
    
    private String getPathFreeClassName(String classname){
        if (classname != null && classname.contains(".")){
            classname = classname.substring(classname.lastIndexOf(".") + 1);
        }
        return classname;
    }

}
