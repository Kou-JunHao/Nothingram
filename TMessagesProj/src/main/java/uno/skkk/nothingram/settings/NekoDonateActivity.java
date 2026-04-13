package uno.skkk.nothingram.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.TextHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uno.skkk.nothingram.helpers.remote.ConfigHelper.Crypto;

public class NekoDonateActivity extends BaseNekoSettingsActivity {
    private final List<Crypto> cryptos = buildStaticCryptos();

    private final int cryptoRow = 200;
    private static List<Crypto> buildStaticCryptos() {
        ArrayList<Crypto> list = new ArrayList<>(7);
        Crypto eth = new Crypto();
        eth.currency = "ETH";
        eth.chain = "ERC20";
        eth.address = "0xA7B4B0cEBfE770C59Ca907A48d48a47c12868ae2";
        list.add(eth);

        Crypto bnb = new Crypto();
        bnb.currency = "BNB";
        bnb.chain = "BEP20";
        bnb.address = "0xA7B4B0cEBfE770C59Ca907A48d48a47c12868ae2";
        list.add(bnb);

        Crypto arb = new Crypto();
        arb.currency = "ETH";
        arb.chain = "ARB";
        arb.address = "0xA7B4B0cEBfE770C59Ca907A48d48a47c12868ae2";
        list.add(arb);

        Crypto usdt = new Crypto();
        usdt.currency = "USDT";
        usdt.chain = "TRC20";
        usdt.address = "TVE6CddubC7tKczLvKSudsfu53sGrgTQTZ";
        list.add(usdt);

        Crypto ton = new Crypto();
        ton.currency = "TON";
        ton.chain = "TON";
        ton.address = "UQAf_Al4PQbh4VN78BEz-slcAmwTbrKLKxKUY1BTTk7-mk7G";
        list.add(ton);

        Crypto sol = new Crypto();
        sol.currency = "SOL";
        sol.chain = "SOL";
        sol.address = "9gCZLGfUuMiPHGthy8qhfJH72sarLNYUvWkhbU2VoEiq";
        list.add(sol);

        Crypto btc = new Crypto();
        btc.currency = "BTC";
        btc.chain = "BTC";
        btc.address = "1KziXBPZdD2yDjhkAnCEbikGTAR526386a";
        list.add(btc);
        return list;
    }


    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (cryptos != null && !cryptos.isEmpty()) {
            items.add(UItem.asHeader(LocaleController.getString(R.string.Cryptocurrency)));
            var cryptoId = 0;
            for (var crypto : cryptos) {
                items.add(TextDetailSettingsCellFactory.of(cryptoRow + cryptoId++, String.format("%s (%s)", crypto.currency, crypto.chain), crypto.address));
            }
            items.add(UItem.asShadow(null));
        }
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        var id = item.id;
        if (id >= cryptoRow) {
            Crypto crypto = cryptos.get(id - cryptoRow);
            QRCodeBottomSheet.showForCrypto(this, crypto);
        }
    }

    @Override
    protected boolean onItemLongClick(UItem item, View view, int position, float x, float y) {
        var id = item.id;
        if (id >= cryptoRow) {
            Crypto crypto = cryptos.get(id - cryptoRow);
            ItemOptions.makeOptions(this, view)
                    .setScrimViewBackground(listView.getClipBackground(view))
                    .add(R.drawable.msg_qrcode, LocaleController.getString(R.string.GetQRCode), () -> QRCodeBottomSheet.showForCrypto(this, crypto))
                    .add(R.drawable.msg_copy, LocaleController.getString(R.string.Copy), () -> {
                        AndroidUtilities.addToClipboard(crypto.address);
                        BulletinFactory.of(this).createCopyBulletin(LocaleController.getString(R.string.TextCopied)).show();
                    })
                    .setMinWidth(190)
                    .show();
            return true;
        }
        return super.onItemLongClick(item, view, position, x, y);
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.Donate);
    }

    public static class QRCodeBottomSheet extends BottomSheet {

        private QRCodeBottomSheet(Context context, Crypto crypto, Theme.ResourcesProvider resourcesProvider) {
            super(context, false, resourcesProvider);

            fixNavigationBar(getThemedColor(Theme.key_windowBackgroundGray));
            setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));

            var linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            var titleView = TextHelper.makeTextView(context, 20, Theme.key_windowBackgroundWhiteBlackText, true);
            titleView.setText(crypto.currency);
            linearLayout.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 6, 0, 0));

            var subTitleView = TextHelper.makeTextView(context, 14, Theme.key_windowBackgroundWhiteGrayText, false);
            subTitleView.setText(crypto.chain);
            linearLayout.addView(subTitleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 6, 0, 6));

            var imageContainer = new LinearLayout(context);
            imageContainer.setOrientation(LinearLayout.VERTICAL);
            imageContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            imageContainer.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
            imageContainer.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(16), Theme.getColor(Theme.key_windowBackgroundWhite)));
            linearLayout.addView(imageContainer, LayoutHelper.createLinear(220 + 16, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 18, 0, 18, 0));

            var imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight(), AndroidUtilities.dp(12));
                }
            });
            imageView.setClipToOutline(true);
            imageView.setImageBitmap(createQR(crypto.address));
            imageContainer.addView(imageView, LayoutHelper.createLinear(220, 220));

            View.OnClickListener copy = (v) -> {
                AndroidUtilities.addToClipboard(crypto.address);
                getBulletinFactory().createCopyBulletin(LocaleController.getString(R.string.TextCopied)).show();
            };

            var addressView = TextHelper.makeTextView(context, 12, Theme.key_windowBackgroundWhiteGrayText, false);
            addressView.setText(crypto.address);
            addressView.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
            addressView.setBackground(Theme.createRadSelectorDrawable(getThemedColor(Theme.key_listSelector), 8, 8));
            addressView.setOnClickListener(copy);
            imageContainer.addView(addressView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 0));

            var copyView = new ButtonWithCounterView(context, true, resourcesProvider).setRound();
            copyView.setText(LocaleController.getString(R.string.Copy), false);
            copyView.setOnClickListener(copy);
            linearLayout.addView(copyView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM, 16, 16, 16, 16));

            var scrollView = new ScrollView(context);
            scrollView.addView(linearLayout);
            setCustomView(scrollView);
        }

        private Bitmap createQR(String key) {
            try {
                HashMap<EncodeHintType, Object> hints = new HashMap<>();
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
                hints.put(EncodeHintType.MARGIN, 0);
                var writer = new QRCodeWriter();
                return writer.encode(key, 768, 768, hints, null, 1.0f, 0xffffffff, 0xff000000, false);
            } catch (Exception e) {
                FileLog.e(e);
            }
            return null;
        }

        public static void showForCrypto(BaseFragment fragment, Crypto crypto) {
            new QRCodeBottomSheet(fragment.getParentActivity(), crypto, fragment.getResourceProvider()).show();
        }
    }
}
