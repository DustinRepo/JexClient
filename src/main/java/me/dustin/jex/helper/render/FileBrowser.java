package me.dustin.jex.helper.render;

import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.util.ArrayList;

public class FileBrowser {

    private float x, y, width, height;
    private String path;
    private long lastClick = 0;

    private boolean multiSelect;

    private String[] filter;

    private final ArrayList<FileElement> fileElements = new ArrayList<>();

    private Scrollbar scrollbar;
    private boolean movingScrollbar;
    private final ButtonListener doubleClickListener;

    public FileBrowser(String openPath, float x, float y, float width, float height, ButtonListener doubleClickListener) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.doubleClickListener = doubleClickListener;
        path = openPath;
        loadFileElements();
        scrollbar = new Scrollbar(x + width - 6, y + 1, 5, height - 2, height - 2, this.getFileElements().size() * 20, 0xffffffff);
    }

    public FileBrowser(String openPath, float x, float y, float width, float height, ButtonListener doubleClickListener, String... filter) {
        this(openPath, x,y,width,height, doubleClickListener);
        path = openPath;
        this.setFilter(filter);
        loadFileElements();
    }

    public void render(PoseStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, x, y, x + width, y + height, 0x90696969);
        Scissor.INSTANCE.cut((int)x, (int)y + 1, (int)width, (int)height - 2);
        fileElements.forEach(fileElement -> fileElement.render(matrixStack));
        Scissor.INSTANCE.seal();
        scrollbar.setContentHeight(fileElements.size() * 20);
        scrollbar.render(matrixStack);
    }

    public void tick() {
        if (movingScrollbar) {
            if (MouseHelper.INSTANCE.isMouseButtonDown(0))
                moveScrollbar();
            else
                movingScrollbar = false;
        }
    }

    public void scroll(double scollOffset) {
        if (fileElements.isEmpty())
            return;
        FileElement first = (FileElement)fileElements.toArray()[0];
        FileElement last = (FileElement)fileElements.toArray()[fileElements.size() -1];
        if (scollOffset < 0) {
            for (int i = 0; i < 25; i++) {
                if (last.getY() + last.getHeight() > this.y + this.height - 1) {
                    for (Button button : fileElements) {
                        button.setY(button.getY() - 1);
                    }
                    scrollbar.moveDown();
                }
            }
        } else if (scollOffset > 0) {
            for (int i = 0; i < 25; i++) {
                if (first.getY() < this.getY() + 1) {
                    for (Button button : fileElements) {
                        button.setY(button.getY() + 1);
                    }
                    scrollbar.moveUp();
                }
            }
        }
    }

    boolean hasSelected = false;
    public void click() {
        if (scrollbar.isHovered()) {
            movingScrollbar = true;
        }
        hasSelected = false;
        if (Render2DHelper.INSTANCE.isHovered(x, y, width - 6, height)) {
            for (int i = 0; i < fileElements.size(); i++) {
                FileElement button = fileElements.get(i);
                if (button.isHovered()) {
                    if (button.isSelected()) {
                        if (System.currentTimeMillis() - lastClick < 250) { //Double click
                            if (button.getFile() == null) {
                                loadRoots();
                            } else if (button.getFile().isDirectory()) {
                                boolean loadedRoot = false;
                                if (path.equalsIgnoreCase(button.getFile().getPath())) {
                                    loadRoots();
                                    loadedRoot = true;
                                }
                                if (!loadedRoot)
                                    for (File f : File.listRoots()) {
                                        if (f.getPath().equalsIgnoreCase(button.getFile().getPath())) {
                                            path = f.getPath();
                                            loadRoot(f);
                                            loadedRoot = true;
                                        }
                                    }

                                path = button.getFile().getPath();
                                if (!loadedRoot)
                                    loadFileElements();
                            } else {
                                if (doubleClickListener != null) {
                                    doubleClickListener.invoke();
                                }
                            }
                        }
                    }
                    if (!multiSelect) {
                        if (!hasSelected) {
                            button.setSelected(true);
                            hasSelected = true;
                        }
                    } else
                        button.setSelected(true);
                } else {
                    if (!multiSelect || KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_CONTROL))
                        button.setSelected(false);

                    if (!multiSelect && hasSelected)
                        button.setSelected(false);
                }
            }
            lastClick = System.currentTimeMillis();
        }
    }

    private void moveScrollbar() {
        float mouseY = MouseHelper.INSTANCE.getMouseY();
        float scrollBarHoldingArea = scrollbar.getY() + (scrollbar.getHeight() / 2.f);
        float dif = mouseY - scrollBarHoldingArea;
        if (dif > 1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() + scrollbar.getHeight() < scrollbar.getViewportY() + scrollbar.getViewportHeight()) {
                    scrollbar.moveDown();
                    for (FileElement fileElement : this.getFileElements()) {
                        fileElement.setY(fileElement.getY() - 1);
                    }
                }
            }
        } else if (dif < -1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() > scrollbar.getViewportY()) {
                    scrollbar.moveUp();
                    for (FileElement fileElement : this.getFileElements()) {
                        fileElement.setY(fileElement.getY() + 1);
                    }
                }
            }
        }
    }

    public String getPath() {
        return path;
    }

    public ArrayList<File> getSelectedFiles() {
        ArrayList<File> files = new ArrayList<>();
        for (FileElement fileElement : this.getFileElements()) {
            if (fileElement.isSelected())
                files.add(fileElement.getFile());
        }
        return files;
    }

    public void loadRoot(File file) {
        this.getFileElements().clear();
        int count = 0;

        this.getFileElements().add(new FileElement(file, this.getX() + 1, this.getY() + 1, this.getWidth() - 2, 20));
        count = 1;

        for (File file1 : file.listFiles()) {
            if (this.getFilter() != null) {
                if (!file1.isDirectory()) {
                    if (!contains(file1.getName(), filter))
                        continue;
                }
            }
            this.getFileElements().add(new FileElement(file1, this.getX() + 1, this.getY() + 1 + (20 * count), this.getWidth() - 2, 20));
            count++;
        }
        scrollbar = new Scrollbar(x + width - 5, y, 5, height - 2, height - 2, this.getFileElements().size() * 20, 0xffffffff);
    }

    public void loadFileElements() {
        this.fileElements.clear();
        File file = new File(path);
        if (!file.isDirectory())
            file = file.getParentFile();

        int count = 0;

        if (file.getParentFile() != null || isRoot(path)) {
            FileElement element = new FileElement(file.getParentFile(), this.getX() + 1, this.getY() + 1, this.getWidth() - 2, 20);
            element.setName("\\..");
            this.getFileElements().add(element);
            count = 1;
        } else {
            loadRoots();
            return;
        }
        for (File file1 : file.listFiles()) {
            if (this.getFilter() != null) {
                if (!file1.isDirectory()) {
                    if (!contains(file1.getName(), filter))
                        continue;
                }
            }
            this.getFileElements().add(new FileElement(file1, this.getX() + 1, this.getY() + 1 + (20 * count), this.getWidth() - 2, 20));
            count++;
        }
        scrollbar = new Scrollbar(x + width - 6, y + 1, 5, height - 2, height - 2, this.getFileElements().size() * 20, 0xffffffff);
    }

    private boolean contains(String s, String[] filter) {
        for (String str : filter) {
            if (s.toLowerCase().endsWith(str.toLowerCase()))
                return true;
        }
        return false;
    }

    public void loadRoots() {
        this.fileElements.clear();
        int count = 0;
        for (File file : File.listRoots()) {
            this.getFileElements().add(new FileElement(file, this.getX() + 1, this.getY() + 1 + (20 * count), this.getWidth() - 2, 20));
            count++;
        }
    }

    public boolean isRoot(String fileName) {
        for (File file : File.listRoots()) {
            if (file.getPath().equalsIgnoreCase(fileName))
                return true;
        }
        return false;
    }

    public ArrayList<FileElement> getFileElements() {
        return fileElements;
    }

    public String[] getFilter() {
        return filter;
    }

    public void setFilter(String[] filter) {
        this.filter = filter;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public static class FileElement extends Button {
        private ResourceLocation folderTex = new ResourceLocation("jex", "gui/jex/folder.png");
        private File file;
        private boolean isSelected;

        public FileElement(File file, float x, float y, float width, float height) {
            super(file == null ? "\\.." : (file.getName().equalsIgnoreCase("") ? file.getPath() : file.getName()), x, y, width, height,null);
            this.file = file;
        }

        @Override
        public void render(PoseStack matrixStack) {
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), isSelected() ? 0x80999999 : 0x80151515);
            FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getName(), getX() + 20, getY() + (getHeight() / 2) - 4.5f, isEnabled() ? -1 : 0xff454545);

            if (file == null || file.isDirectory()) {
                Render2DHelper.INSTANCE.bindTexture(folderTex);
                Render2DHelper.INSTANCE.drawTexture(matrixStack, getX() + 1, getY() + 1, 0, 0,18, 18, 18, 18);
            }
        }

        public File getFile() {
            return file;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }
}
