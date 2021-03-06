/*******************************************************************************
 * MIT License
 * 
 * Copyright (c) 2016 Raymond Buckley
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.ray3k.skincomposer.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.ray3k.skincomposer.panel.PanelClassBar;
import com.ray3k.skincomposer.panel.PanelMenuBar;
import com.ray3k.skincomposer.panel.PanelPreviewProperties;
import com.ray3k.skincomposer.panel.PanelStyleProperties;

public class ProjectData implements Json.Serializable{
    private static ProjectData instance;
    private static Preferences generalPref;
    private ObjectMap<String, Object> preferences;
    private JsonData jsonData;
    private AtlasData atlasData;
    private FileHandle saveFile;
    private boolean changesSaved;
    private boolean newProject;
    private static final int MAX_RECENT_FILES = 5;
    
    public static ProjectData instance() {
        if (instance == null) {
            instance = new ProjectData();
        }
        return instance;
    }
    
    private ProjectData() {
        changesSaved = false;
        newProject = true;
        preferences = new ObjectMap<>();
        generalPref = Gdx.app.getPreferences("com.ray3k.skincomposer");
        jsonData = JsonData.getInstance();
        atlasData = AtlasData.getInstance();
    }
    
    public int getId() {
        return (int) preferences.get("id");
    }
    
    public void setId(int id) {
        preferences.put("id", id);
    }
    
    public void randomizeId() {
        int id = MathUtils.random(100000000, 999999999);
        setId(id);
    }
    
    public void setLastDirectory(String lastDirectory) {
        preferences.put("last-directory", lastDirectory);
        generalPref.putString("last-directory", lastDirectory);
        generalPref.flush();
    }
    
    public String getLastDirectory() {
        return (String) preferences.get("last-directory", generalPref.getString("last-directory", Gdx.files.getLocalStoragePath()));
    }
    
    public Array<String> getRecentFiles() {
        Array<String> returnValue = new Array<>();
        int maxIndex = Math.min(MAX_RECENT_FILES, generalPref.getInteger("recentFilesCount", 0));
        for (int i = 0; i < maxIndex; i++) {
            returnValue.add(generalPref.getString("recentFile" + i));
        }
        
        return returnValue;
    }
    
    public void putRecentFile(String filePath) {
        boolean exists = false;
        for (String path : getRecentFiles()) {
            if (path.equals(filePath)) {
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            int index = generalPref.getInteger("recentFilesCount", 0);

            if (index >= MAX_RECENT_FILES) {
                for (int i = 1; i < MAX_RECENT_FILES; i++) {
                    String value = generalPref.getString("recentFile" + i);
                    generalPref.putString("recentFile" + (i - 1), value);
                }
                generalPref.remove("recentFile" + (MAX_RECENT_FILES - 1));

                index = MAX_RECENT_FILES - 1;
            }
            generalPref.putString("recentFile" + index, filePath);
            generalPref.putInteger("recentFilesCount", index + 1);
            generalPref.flush();
        }
        
        PanelMenuBar.instance().getRecentFilesButton().setDisabled(false);
    }
    
    public void setMaxTextureDimensions(int width, int height) {
        preferences.put("texture-max-width", width);
        preferences.put("texture-max-height", height);
    }
    
    public int getMaxTextureWidth() {
        return (int) preferences.get("texture-max-width", 1024);
    }
    
    public int getMaxTextureHeight() {
        return (int) preferences.get("texture-max-height", 1024);
    }
    
    public void setMaxUndos(int maxUndos) {
        preferences.put("maxUndos", maxUndos);
    }
    
    public int getMaxUndos() {
        return (int) preferences.get("maxUndos", 30);
    }
    
    public FileHandle getSaveFile() {
        return saveFile;
    }
    
    public String getBestSaveDirectory() {
        if (saveFile != null) {
            return saveFile.parent().path();
        } else if (generalPref.contains("last-save-directory")) {
            return generalPref.getString("last-save-directory");
        } else {
            return getLastDirectory();
        }
    }

    public boolean areChangesSaved() {
        return changesSaved;
    }

    public void setChangesSaved(boolean changesSaved) {
        this.changesSaved = changesSaved;
        newProject = false;
        String title = "Skin Composer";
        if (saveFile != null && saveFile.exists()) {
            title += " - " + saveFile.nameWithoutExtension();
            if (!changesSaved) {
                title += "*";
            }
        } else {
            title += " - New Project*";
        }
        Gdx.graphics.setTitle(title);
    }

    public boolean isNewProject() {
        return newProject;
    }
    
    private void moveImportedFiles(FileHandle oldSave, FileHandle newSave) {
        FileHandle tempImportFolder = Gdx.files.local("temp/" + ProjectData.instance().getId() + "_data/");
        FileHandle localImportFolder;
        if (oldSave != null) {
            localImportFolder = oldSave.sibling(oldSave.nameWithoutExtension() + "_data/");
        } else {
            localImportFolder = null;
        }
        FileHandle targetFolder = newSave.sibling(newSave.nameWithoutExtension() + "_data/");
        targetFolder.mkdirs();
        
        for (DrawableData drawableData : AtlasData.getInstance().getDrawables()) {
            if (drawableData.file.exists()) {
                //drawable files in the temp folder
                if (drawableData.file.parent().equals(tempImportFolder)) {
                    drawableData.file.moveTo(targetFolder);
                    drawableData.file = targetFolder.child(drawableData.file.name());
                }
                //drawable files in the folder next to the old save
                else if (localImportFolder != null && !localImportFolder.equals(targetFolder) && drawableData.file.parent().equals(localImportFolder)) {
                    drawableData.file.copyTo(targetFolder);
                    drawableData.file = targetFolder.child(drawableData.file.name());
                }
            }
        }
        
        for (FontData fontData : JsonData.getInstance().getFonts()) {
            if (fontData.file.exists()) {
                //font files in the temp folder
                if (fontData.file.parent().equals(tempImportFolder)) {
                    fontData.file.moveTo(targetFolder);
                    fontData.file = targetFolder.child(fontData.file.name());
                }
                //font files in the data folder next to the old save
                else if (localImportFolder != null && !localImportFolder.equals(targetFolder) && fontData.file.parent().equals(localImportFolder)) {
                    fontData.file.copyTo(targetFolder);
                    fontData.file = targetFolder.child(fontData.file.name());
                }
            }
        }
    }
    
    public void save(FileHandle file) {
        moveImportedFiles(saveFile, file);
        
        saveFile = file;
        putRecentFile(file.path());
        generalPref.putString("last-save-directory", file.parent().path());
        generalPref.flush();
        Json json = new Json(JsonWriter.OutputType.minimal);
        json.setUsePrototypes(false);
        file.writeString(json.prettyPrint(this), false);
        setChangesSaved(true);
    }
    
    public void save() {
        save(saveFile);
    }
    
    public void load(FileHandle file) {
        Json json = new Json(JsonWriter.OutputType.minimal);
        instance = json.fromJson(ProjectData.class, file);
        instance.saveFile = file;
        putRecentFile(file.path());
        generalPref.putString("last-save-directory", file.parent().path());
        generalPref.flush();
        PanelClassBar.instance.populate();
        PanelStyleProperties.instance.populate(PanelClassBar.instance.getStyleSelectBox().getSelected());
        AtlasData.getInstance().atlasCurrent = false;
        PanelPreviewProperties.instance.produceAtlas();
        PanelPreviewProperties.instance.populate();
        instance.setChangesSaved(true);
    }
    
    public void load() {
        load(saveFile);
    }
    
    public void clear() {
        preferences.clear();

        randomizeId();
        setMaxTextureDimensions(1024, 1024);
        setMaxUndos(30);
        
        jsonData.clear();
        atlasData.clear();
        saveFile = null;
        PanelClassBar.instance.populate();
        PanelStyleProperties.instance.populate(PanelClassBar.instance.getStyleSelectBox().getSelected());
        PanelPreviewProperties.instance.produceAtlas();
        PanelPreviewProperties.instance.populate();
        setChangesSaved(false);
        newProject = true;
    }

    @Override
    public void write(Json json) {
        json.writeValue("atlasData", atlasData);
        json.writeValue("jsonData", jsonData);
        json.writeValue("preferences", preferences);
        if (saveFile != null) {
            json.writeValue("saveFile", saveFile.path());
        } else {
            json.writeValue("saveFile", (String) null);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        preferences = json.readValue("preferences", ObjectMap.class, jsonData);
        JsonData.loadInstance(json.readValue("jsonData", JsonData.class, jsonData));
        this.jsonData = JsonData.getInstance();
        AtlasData.loadInstance(json.readValue("atlasData", AtlasData.class, jsonData));
        this.atlasData = AtlasData.getInstance();
        if (!jsonData.get("saveFile").isNull()) {
            saveFile = new FileHandle(jsonData.getString("saveFile"));
        }
    }

    public void setSelectedSkin(int selectedSkin) {
        generalPref.putInteger("selectedSkin", selectedSkin);
        generalPref.flush();
    }
    
    public int getSelectedSkin() {
        return (int) generalPref.getInteger("selectedSkin", 0);
    }

    public boolean getStripWhitespace() {
        return generalPref.getBoolean("useStripWhitespace", false);
    }

    public void setStripWhitespace(boolean useStripWhitespace) {
        generalPref.putBoolean("useStripWhitespace", useStripWhitespace);
        generalPref.flush();
    }
}
