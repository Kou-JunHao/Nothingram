package uno.skkk.nothingram.translator;

import org.telegram.tgnet.TLRPC;

import java.util.List;

import android.text.TextUtils;

public class ServiceTranslator implements Translator.ITranslator {

    private final TextWithEntitiesTranslator delegate;

    private ServiceTranslator(TextWithEntitiesTranslator delegate) {
        this.delegate = delegate;
    }

    public static ServiceTranslator of(String type) {
        return new ServiceTranslator(TextWithEntitiesTranslator.of(type));
    }

    @Override
    public Translator.TranslationResult translate(TLRPC.TL_textWithEntities query, String fl, String tl) throws Exception {
        return delegate.translate(query, fl, tl);
    }

    @Override
    public boolean supportLanguage(String language) {
        return !TextUtils.isEmpty(language) && delegate.supportLanguage(language);
    }

    @Override
    public List<String> getTargetLanguages() {
        return delegate.getTargetLanguages();
    }
}
