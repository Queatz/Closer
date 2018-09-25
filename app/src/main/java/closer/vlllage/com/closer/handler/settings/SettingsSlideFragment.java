package closer.vlllage.com.closer.handler.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.WindowHandler;
import closer.vlllage.com.closer.pool.PoolFragment;

import static closer.vlllage.com.closer.handler.settings.UserLocalSetting.CLOSER_SETTINGS_OPEN_GROUP_EXPANDED;

public class SettingsSlideFragment extends PoolFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_settings, container, false);
        view.findViewById(R.id.scrollView).setPadding(0, $(WindowHandler.class).getStatusBarHeight(), 0, 0);

        Switch openGroupsExpandedSettingsSwitch = view.findViewById(R.id.openGroupsExpandedSettingsSwitch);
        openGroupsExpandedSettingsSwitch.setChecked($(SettingsHandler.class).get(CLOSER_SETTINGS_OPEN_GROUP_EXPANDED));
        openGroupsExpandedSettingsSwitch.setOnCheckedChangeListener((v, checked) -> $(SettingsHandler.class).set(CLOSER_SETTINGS_OPEN_GROUP_EXPANDED, checked));

        view.findViewById(R.id.sendFeedbackButton).setOnClickListener(v -> $(GroupActivityTransitionHandler.class).showGroupMessages(v, "12370162"));
        view.findViewById(R.id.viewPrivacyPolicyButton).setOnClickListener(v -> {
            $(DisposableHandler.class).add($(ApiHandler.class).privacy().subscribe(privacyPolicy -> {
                $(DefaultAlerts.class).message(privacyPolicy);
            }, e -> $(DefaultAlerts.class).thatDidntWork()));
        });

        return view;
    }

}