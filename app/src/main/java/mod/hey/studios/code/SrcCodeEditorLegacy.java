package mod.hey.studios.code;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.ViewCodeBinding;

import pro.sketchware.utility.FileUtil;
import mod.hey.studios.lib.code_editor.CodeEditorLayout;
import mod.hey.studios.lib.code_editor.ColorScheme;

/**
 * Legacy code editor
 */
public class SrcCodeEditorLegacy extends Activity {

    private SharedPreferences sp;
    private ViewCodeBinding binding;

    private final View.OnClickListener changeTextSize = v -> {
        if (v.getId() == R.id.code_editor_zoomin) {
            binding.codeEditor.increaseTextSize();
        } else if (v.getId() == R.id.code_editor_zoomout) {
            binding.codeEditor.decreaseTextSize();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ViewCodeBinding.inflate(getLayoutInflater());
        sp = getSharedPreferences("code_editor_pref", 0);
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("java")) {
            binding.codeEditor.start(ColorScheme.JAVA());
        } else if (getIntent().hasExtra("xml")) {
            binding.codeEditor.start(ColorScheme.XML());
        }

        if (getIntent().hasExtra("title") && getIntent().hasExtra("content")) {
            binding.textTitle.setText(getIntent().getStringExtra("title"));
            binding.codeEditor.setText(FileUtil.readFile(getIntent().getStringExtra("content")));
        }

        binding.codeEditorZoomin.setOnClickListener(changeTextSize);
        binding.codeEditorZoomout.setOnClickListener(changeTextSize);

        binding.codeEditor.onCreateOptionsMenu(findViewById(R.id.codeeditor_more_options));
        binding.codeEditor.getEditText().setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    }

    @Override
    public void onBackPressed() {
        boolean exitConfirmationDialogEnabled = sp.getBoolean("exit_confirmation_dialog", false);
        if (exitConfirmationDialogEnabled) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.save_changes)
                    .setMessage(R.string.do_you_want_to_save_your_changes)
                    .setPositiveButton(R.string.common_word_save, (dialog, which) -> {
                        FileUtil.writeFile(getIntent().getStringExtra("content"), binding.codeEditor.getText());
                        Toast.makeText(this, R.string.file_saved, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNeutralButton(R.string.discard, (dialog, which) -> finish())
                    .setNegativeButton(R.string.common_word_cancel, null)
                    .show();
        } else {
            super.onBackPressed();

            FileUtil.writeFile(getIntent().getStringExtra("content"), binding.codeEditor.getText());
            Toast.makeText(this, R.string.file_saved, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.codeEditor.onPause();
    }
}
