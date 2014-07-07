package no.difi.sdp.util;

import org.mockito.ArgumentMatcher;

import java.io.IOException;
import java.lang.reflect.Field;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public  abstract class AssertValue<T> extends ArgumentMatcher<T> {
    public abstract void asserts(T value) throws IOException;

    @SuppressWarnings({ "deprecation", "unchecked" })
	@Override
    public boolean matches(Object o) {
        try {
            asserts((T) o);
        } catch (IOException e) {
            assertFalse("exception in assert", false);
        }

        return true;
    }

    protected Object privateValue(Object o, String field){

        assert(o != null);

        if(field.contains("."))
            return privateValue(o, field.split("\\."));

        Object val = null;
        
        Class clazz = o.getClass();
        while (val == null && clazz != null){
        	val = readField(o, clazz, field);
        	clazz = clazz.getSuperclass();
        }
        
        if (val != null) {
        	return val;
        }
        
        assertTrue("Cant find accessor " + field, true);
        return null;
    }

    private Object readField(Object o, Class clazz, String field ) {
    	 final Field fields[] = clazz.getDeclaredFields();
         for (int i = 0; i < fields.length; ++i) {
             if (field.equals(fields[i].getName())) {
                 try {
                     fields[i].setAccessible(true);
                     return fields[i].get(o);
                 } catch (IllegalAccessException ex) {
                     assertTrue("Cant access accessor " + field, true);
                 }
             }
         }
         
         return null;
    }
    
    protected Object privateValue(Object o, String[] split) {
        Object current  = o;
        for(String next: split){
            current = privateValue(current, next);
        }

        return current;
    }
}
