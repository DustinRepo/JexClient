package me.dustin.jex.gui.profileedit;

import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public class SecurityQuestionsScreen extends Screen {
    private Screen parent;
    public SecurityQuestionsScreen(Screen parent) {
        super(new LiteralText("Security Questions"));
        this.parent = parent;
    }

    private Map<Integer, String> questions;
    private TextFieldWidget answer1, answer2, answer3;
    private ButtonWidget sendAnswersButton;
    private String errorString = "";

    private boolean answered;

    @Override
    protected void init() {
        questions = MCAPIHelper.INSTANCE.getSecurityQuestions();
        answer1 = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 150, 25, 300, 20, new LiteralText(""));
        answer2 = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 150, 60, 300, 20, new LiteralText(""));
        answer3 = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 150, 95, 300, 20, new LiteralText(""));
        sendAnswersButton = new ButtonWidget(width / 2 - 75, 120, 150, 20, new LiteralText("Send Answers"), button -> {
            Integer[] answerIds = questions.keySet().toArray(Integer[]::new);
            int answer1Id = answerIds[0];
            int answer2Id = answerIds[1];
            int answer3Id = answerIds[2];
            Map<Integer, String> answers = new HashMap<>();
            answers.put(answer1Id, answer1.getText());
            answers.put(answer2Id, answer2.getText());
            answers.put(answer3Id, answer3.getText());

            this.errorString = Formatting.GRAY + "Sending...";

            new Thread(() -> {
                String resp = MCAPIHelper.INSTANCE.sendSecurityAnswers(answers);
                if (resp.equalsIgnoreCase("success")) {
                    answered = true;
                } else {
                    errorString = Formatting.RED + resp;
                }
            }).start();
        });
        this.addDrawableChild(sendAnswersButton);
        this.addSelectableChild(answer1);
        this.addSelectableChild(answer2);
        this.addSelectableChild(answer3);
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        String[] questionStrings = questions.values().toArray(String[]::new);

        answer1.render(matrices, mouseX, mouseY, delta);
        answer2.render(matrices, mouseX, mouseY, delta);
        answer3.render(matrices, mouseX, mouseY, delta);

        FontHelper.INSTANCE.drawCenteredString(matrices, questionStrings[0], width / 2.f, answer1.y - 10, -1);
        FontHelper.INSTANCE.drawCenteredString(matrices, questionStrings[1], width / 2.f, answer2.y - 10, -1);
        FontHelper.INSTANCE.drawCenteredString(matrices, questionStrings[2], width / 2.f, answer3.y - 10, -1);
        FontHelper.INSTANCE.drawCenteredString(matrices, errorString, width / 2.f, sendAnswersButton.y + sendAnswersButton.getHeight() + 10, -1);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        answer1.tick();
        answer2.tick();
        answer3.tick();
        this.sendAnswersButton.active = !answer1.getText().isEmpty() && !answer2.getText().isEmpty() && !answer3.getText().isEmpty();
        if (answered) {
            Wrapper.INSTANCE.getMinecraft().openScreen(new ProfileEditingScreen(parent));
        }
        super.tick();
    }
}
