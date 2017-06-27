package ga.ai.mobile.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;

import ga.ai.mobile.R;

public class PoputchikiFragment extends EntriesListFragment {


    @Override
    protected void createFAB(View rootView) {
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Размещение объявления")
                        .setMessage("Вы можете разместить свое объявление в ленте \"Попутчики РА\".\n\n" +
                                "Объявление принимается с вашего телефона как СМС-сообщение на алтайский номер МТС (согласно стоимости одного СМС сообщения на вашем тарифном плане). " +
                                "Публикация может занять некоторое время (несколько часов).\n\n" +
                                "При составлении объявлений, пожалуйста, придерживайтесь общего правила: если Вы ищете попутчика, начинайте текст как \"Водитель\", " +
                                "если ищете машину, то как \"Пассажир\". Затем укажите маршрут и ваш номер телефона.")
                        .setIcon(R.drawable.ic_edit_black_24dp)
                        .setCancelable(false)
                        .setPositiveButton("Подать объявление",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", "89139914754", null)));
                                    dialog.cancel();
                                }
                            })
                        .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                AlertDialog alert = builder.create();
                alert.show();

//                Toast.makeText(getActivity(), "Короткое нажатие", Toast.LENGTH_LONG).show();
            }
        });
//        UiUtils.updateHideReadButton(getActivity(), fab);
    }


//    protected void onClickFAB(View view) {
//        if (!PrefUtils.getBoolean(PrefUtils.SHOW_READ, true)) {
//            PrefUtils.putBoolean(PrefUtils.SHOW_READ, true);
//            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_pink)));
//            fab.setImageResource(R.drawable.ic_visibility);
//        } else {
//            PrefUtils.putBoolean(PrefUtils.SHOW_READ, false);
//            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_gray)));
//            fab.setImageResource(R.drawable.ic_visibility_off);
//        }
//    }
}
