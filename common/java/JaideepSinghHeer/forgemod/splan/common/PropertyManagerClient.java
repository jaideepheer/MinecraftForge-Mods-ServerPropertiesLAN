package JaideepSinghHeer.forgemod.splan.common;

import java.io.*;
import java.util.Properties;

import static JaideepSinghHeer.forgemod.splan.common.Context.LOGGER;

/**
 * This Class handles all the IO of the server.properties file in the world's folder.
 */
public class PropertyManagerClient
{
    /** The server properties object. */
    private final Properties serverProperties = new Properties();
    /** The server properties file. */
    private File serverPropertiesFile;

    public String comment;

    public PropertyManagerClient(File propertiesFile)
    {
        this.serverPropertiesFile = propertiesFile;
        this.comment = "Minecraft server properties";
        if (propertiesFile.exists())
        {
            FileInputStream fileinputstream = null;
            try
            {
                fileinputstream = new FileInputStream(propertiesFile);
                this.serverProperties.load((InputStream)fileinputstream);
            }
            catch (Exception exception)
            {
                LOGGER.warn("Failed to load {}", new Object[] {propertiesFile, exception});
                this.generateNewProperties();
            }
            finally
            {
                if (fileinputstream != null)
                {
                    try
                    {
                        fileinputstream.close();
                    }
                    catch (IOException var11)
                    {
                        ;
                    }
                }
            }
        }
        else
        {
            LOGGER.warn("{} does not exist", new Object[] {propertiesFile});
            this.generateNewProperties();
        }
    }

    /**
     * Generates a new properties file.
     */
    public void generateNewProperties()
    {
        LOGGER.info("Generating new properties file");
        this.serverPropertiesFile.delete();
        try {
            this.serverPropertiesFile.createNewFile();
            this.saveProperties();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warn("Failed create new properties file "+this.serverPropertiesFile.getAbsolutePath());
        }
    }

    /**
     * Writes the properties to the properties file.
     */
    public void saveProperties()
    {
        FileOutputStream fileoutputstream = null;

        try
        {
            fileoutputstream = new FileOutputStream(this.serverPropertiesFile);
            this.serverProperties.store((OutputStream)fileoutputstream, comment);
        }
        catch (Exception exception)
        {
            LOGGER.warn("Failed to save {}", new Object[] {this.serverPropertiesFile, exception});
            this.generateNewProperties();
        }
        finally
        {
            if (fileoutputstream != null)
            {
                try
                {
                    fileoutputstream.close();
                }
                catch (IOException var10)
                {
                    ;
                }
            }
        }
    }

    /**
     * Returns this PropertyManager's file object used for property saving.
     */
    public File getPropertiesFile()
    {
        return this.serverPropertiesFile;
    }

    /**
     * Sets this PropertyManager's file object.
     */
    public void setPropertiesFile(File f){this.serverPropertiesFile = f;}

    /**
     * Returns a string property. If the property doesn't exist the default is returned.
     */
    public String getStringProperty(String key, String defaultValue)
    {
        if (!this.serverProperties.containsKey(key))
        {
            this.serverProperties.setProperty(key, defaultValue);
            this.saveProperties();
            this.saveProperties();
        }

        return this.serverProperties.getProperty(key, defaultValue);
    }

    /**
     * Gets an integer property. If it does not exist, set it to the specified value.
     */
    public int getIntProperty(String key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(this.getStringProperty(key, "" + defaultValue));
        }
        catch (Exception var4)
        {
            this.serverProperties.setProperty(key, "" + defaultValue);
            this.saveProperties();
            return defaultValue;
        }
    }

    public long getLongProperty(String key, long defaultValue)
    {
        try
        {
            return Long.parseLong(this.getStringProperty(key, "" + defaultValue));
        }
        catch (Exception var5)
        {
            this.serverProperties.setProperty(key, "" + defaultValue);
            this.saveProperties();
            return defaultValue;
        }
    }

    /**
     * Gets a boolean property. If it does not exist, set it to the specified value.
     */
    public boolean getBooleanProperty(String key, boolean defaultValue)
    {
        try
        {
            return Boolean.parseBoolean(this.getStringProperty(key, "" + defaultValue));
        }
        catch (Exception var4)
        {
            this.serverProperties.setProperty(key, "" + defaultValue);
            this.saveProperties();
            return defaultValue;
        }
    }

    /**
     * Saves an Object with the given property name.
     */
    public void setProperty(String key, Object value)
    {
        this.serverProperties.setProperty(key, "" + value);
    }

    public boolean hasProperty(String key)
    {
        return this.serverProperties.containsKey(key);
    }

    public void removeProperty(String key)
    {
        this.serverProperties.remove(key);
    }

}