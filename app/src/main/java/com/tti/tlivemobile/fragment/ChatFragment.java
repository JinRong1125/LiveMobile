package com.tti.tlivemobile.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.opendanmaku.DanmakuItem;
import com.opendanmaku.DanmakuView;
import com.tti.tlivelibrary.tliveservice.constants.Constants;
import com.tti.tlivelibrary.tliveservice.request.QueryEmojiLinksRequest;
import com.tti.tlivelibrary.tliveservice.response.QueryEmojiLinksResponse;
import com.tti.tlivemobile.R;

import com.tti.tlivemobile.adapter.EmotionAdapter;
import com.tti.tlivemobile.adapter.MessageAdapter;
import com.tti.tlivemobile.application.TliveMoblie;
import com.tti.tlivemobile.constant.AppConstants;
import com.tti.tlivemobile.manager.SpiceServiceManager;
import com.tti.tlivemobile.model.Emotion;
import com.tti.tlivemobile.model.Message;
import com.tti.tlivemobile.model.Platform;
import com.tti.tlivemobile.utils.Utils;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by dylan_liang on 2017/6/16.
 */

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";

    private View view;
    private RelativeLayout headLayout;
    private FrameLayout chatConatiner, chatLayout;
    private LinearLayout messageLayout;

    private RecyclerView messagesView;
    private GridView emotionsView;
    private TextView connectText, viewersValue;
    private EditText messageEdit;
    private ImageButton sendButton, emotionButton;

    private DanmakuView danmakuView;

    private RecyclerView.Adapter messageAdapter;
    private EmotionAdapter emotionAdapter;

    private SpiceManager spiceManager;
    private QueryEmojiLinksRequest queryEmojiLinksRequest;

    private ChatHandler chatHandler;

    private Activity activity;

    private List<Message> messageList;
    private List<Emotion> emotionList;
    private boolean isChatDisplay;
    private boolean isConnected;
    private String userName;
    private String chatName;

    private Socket socket;

    private static final int CHAT_ALPHA = 225;
    private static final int CHAT_SHOW = 0;
    private static final int CHAT_HIDE = 1;

    private static final String EMIT_JOIN = "join";
    private static final String EMIT_MSG = "message";
    private static final String EMIT_DANMAKU = "postDanmaku";
    private static final String EMIT_LEAVE = "leave";
    private static final String ON_ACCESS = "access";
    private static final String ON_SYS = "sys";
    private static final String ON_MSG = "msg";

    private static final String IMG_URL_FRONT = " <img src=\"";
    private static final String IMG_URL_BACK = "\"> ";

    private static final String DANMAKU_COLOR = "#ffffff";
    private static final String DANMAKU_DIRECTION = "right";

    public static Fragment newInstance() {
        return new ChatFragment();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        messageList = new ArrayList<Message>();

        if (context instanceof Activity) {
            activity = (Activity) context;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.view = view;
        isConnected = false;
        initView();
        switchChatType();
        connect();
    }

    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off();
        danmakuView.clear();
    }

    private void initView() {
        headLayout = (RelativeLayout) view.findViewById(R.id.head_layout);
        headLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isChatDisplay)
                    chatHandler.sendEmptyMessage(CHAT_SHOW);
                else
                    chatHandler.sendEmptyMessage(CHAT_HIDE);
            }
        });

        chatConatiner = (FrameLayout) getActivity().findViewById(R.id.chat_container);
        chatLayout = (FrameLayout) view.findViewById(R.id.chat_layout);
        messageLayout = (LinearLayout) view.findViewById(R.id.message_layout);

        connectText = (TextView) view.findViewById(R.id.connect_text);
        viewersValue = (TextView) view.findViewById(R.id.viewers_value);

        messagesView = (RecyclerView) view.findViewById(R.id.messages_view);
        messagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        messageAdapter = new MessageAdapter(activity, messageList);
        messagesView.setAdapter(messageAdapter);

        emotionsView = (GridView) view.findViewById(R.id.emotions_view);
        emotionsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                messageEdit.append(emotionList.get(position).getEmotionCode());
            }
        });

        messageEdit = (EditText) view.findViewById(R.id.message_edit);
        messageEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    if (isConnected) {
                        attemptSend();
                        Utils.hideKeyView(getActivity(), view);
                    }
                    return true;
                }
                return false;
            }
        });

        emotionButton = (ImageButton) view.findViewById(R.id.emotion_button);
        emotionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emotionsView.getVisibility() != View.VISIBLE) {
                    emotionsView.setVisibility(View.VISIBLE);
                    messagesView.setVisibility(View.GONE);
                }
                else {
                    emotionsView.setVisibility(View.GONE);
                    messagesView.setVisibility(View.VISIBLE);
                }
            }
        });

        sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    attemptSend();
                    Utils.hideKeyView(getActivity(), view);
                }
            }
        });

        KeyboardVisibilityEvent.setEventListener(getActivity(), new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (AppConstants.CHAT_TYPE == AppConstants.CHAT_PUBLISH && !isOpen)
                    hideBar();
            }
        });

        danmakuView = (DanmakuView) getActivity().findViewById(R.id.danmaku_layout);
        danmakuView.show();

        spiceManager = SpiceServiceManager.getInstance().getSpiceManager();
        queryEmojiLinks();

        chatHandler = new ChatHandler();
    }

    private void switchChatType() {
        userName = Utils.Preferences.getAccount().getUserName();

        switch (AppConstants.CHAT_TYPE) {
            case AppConstants.CHAT_PUBLISH:
                chatName = userName;

                headLayout.getBackground().setAlpha(CHAT_ALPHA);
                chatLayout.getBackground().setAlpha(CHAT_ALPHA);
                messageLayout.getBackground().setAlpha(CHAT_ALPHA);

                chatHandler.sendEmptyMessage(CHAT_HIDE);
                headLayout.setClickable(true);
                isChatDisplay = false;
                break;
            case AppConstants.CHAT_WATCH:
                Platform platform = getActivity().getIntent().getParcelableExtra(AppConstants.PLATFORM_ITEM);
                chatName = platform.getUserName();

                headLayout.setClickable(false);
                break;
        }
    }

    private class ChatHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CHAT_SHOW:
                    showChat();
                    break;
                case CHAT_HIDE:
                    hideChat();
                    break;
            }
        }
    }

    private void showChat() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) chatConatiner.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.weight = 1;
        chatLayout.setVisibility(View.VISIBLE);
        messageLayout.setVisibility(View.VISIBLE);
        isChatDisplay = true;
    }

    private void hideChat() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) chatConatiner.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.weight = 0;
        chatLayout.setVisibility(View.GONE);
        messageLayout.setVisibility(View.GONE);
        Utils.hideKeyView(getActivity(), view);
        isChatDisplay = false;
    }

    private void hideBar() {
        if (getActivity() != null) {
            View decorView = getActivity().getWindow().getDecorView();
            int UIoptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(UIoptions);
        }
    }

    private void connect() {
        TliveMoblie application = (TliveMoblie) getActivity().getApplication();
        socket = application.getSocket();
        socket.connect();
        socket.on(ON_ACCESS, onAccessMessage);
        socket.on(ON_SYS, onSystemMessage);
        socket.on(ON_MSG, onNewMessage);
        socket.emit(EMIT_JOIN, userName, chatName, Utils.Preferences.getSessionToken());
    }

    private void attemptSend() {
        String message = messageEdit.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            messageEdit.requestFocus();
            return;
        }

        messageEdit.setText("");
        socket.emit(EMIT_MSG, message);
        socket.emit(EMIT_DANMAKU, message, DANMAKU_COLOR, DANMAKU_DIRECTION);

        if (emotionsView.getVisibility() == View.VISIBLE) {
            emotionsView.setVisibility(View.GONE);
            messagesView.setVisibility(View.VISIBLE);
        }
    }

    private void addLog(String message) {
        messageList.add(new Message.Builder(Message.TYPE_LOG).message(message).build());
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void filterEmotionCode(String username, String message) {
        String messageText = message;
        String danmakuText = message;
        boolean isEmoted = false;

        for (int i = 0; i < emotionList.size(); i++) {
            String emotionCode = emotionList.get(i).getEmotionCode();
            if (message.contains(emotionCode)) {
                messageText = messageText.replace(
                        emotionCode, IMG_URL_FRONT + emotionList.get(i).getEmotionPath() + IMG_URL_BACK);
                danmakuText = danmakuText.replace(emotionCode, "");

                if (!isEmoted)
                    isEmoted = true;
            }
        }

        addMessage(username, messageText, isEmoted);
        addDanmaku(danmakuText);
    }

    private void addMessage(String username, String message, boolean isEmoted) {
        messageList.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).isEmoted(isEmoted).build());
        messageAdapter.notifyItemInserted(messageList.size() - 1);

        scrollToBottom();
    }

    private void addDanmaku(String message) {
        if (!TextUtils.isEmpty(message))
            danmakuView.addItem(new DanmakuItem(activity, message, danmakuView.getWidth()));
    }

    private void removeTyping(String username) {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            Message message = messageList.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
                messageList.remove(i);
                messageAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void scrollToBottom() {
        messagesView.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    private void setEmotionList(QueryEmojiLinksResponse response) {
        for (int i = 0; i < response.emojiLinksList.size(); i++) {
            Emotion emotion = new Emotion();
            emotion.setEmotionId(response.emojiLinksList.get(i).emojiId);
            emotion.setEmotionCode(response.emojiLinksList.get(i).emojiCmd);
            emotion.setEmotionPath(response.emojiLinksList.get(i).emojiPath);
            emotionList.add(emotion);

            sortEmotionId(i);
        }

        if (emotionList.size() != 0)
            setEmotions();
    }

    private void setEmotions() {
        emotionAdapter = new EmotionAdapter(activity, emotionList);
        emotionsView.setAdapter(emotionAdapter);
    }

    private void sortEmotionId(int base) {
        if (base > 0) {
            for (int i = base; i > 0; i--) {
                if (emotionList.get(base).getEmotionId() < emotionList.get(i - 1).getEmotionId()) {
                    Emotion emotion = emotionList.get(base);
                    emotionList.set(i, emotionList.get(i - 1));
                    emotionList.set(i - 1, emotion);
                    base--;
                }
                else
                    break;
            }
        }
    }

    private void queryEmojiLinks() {
        emotionList = new ArrayList<>();

        queryEmojiLinksRequest = new QueryEmojiLinksRequest();
        spiceManager.execute(queryEmojiLinksRequest, QueryEmojiLinksListener);
    }

    private RequestListener<QueryEmojiLinksResponse> QueryEmojiLinksListener = new RequestListener<QueryEmojiLinksResponse>() {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Utils.requestFailure("QueryEmojiLinks");
        }

        @Override
        public void onRequestSuccess(QueryEmojiLinksResponse response) {
            if (response == null) {
                Utils.requestFailure("QueryEmojiLinks");
                return;
            }

            if (response.Code == Constants.QUERY_SUCCESS)
                setEmotionList(response);
        }
    };

    private Emitter.Listener onAccessMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int code = (int) args[0];
                    String message = (String) args[1];
                    JSONArray viewerList = (JSONArray) args[2];
                    int viewerCount = viewerList.length();

                    if (code == 0) {
                        if (connectText.getVisibility() == View.VISIBLE)
                            connectText.setVisibility(View.GONE);
                        if (viewerCount > 0)
                            viewersValue.setText(String.valueOf(viewerCount));
                        else
                            viewersValue.setText("1");
                        isConnected = true;
                    }
                    else {
                        if (connectText.getVisibility() == View.GONE)
                            connectText.setVisibility(View.VISIBLE);
                        viewersValue.setText("");
                        isConnected = false;
                        socket.emit(EMIT_JOIN, userName, chatName, Utils.Preferences.getSessionToken());
                    }

                }
            });
        }
    };

    private Emitter.Listener onSystemMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String sysMsg = (String) args[0];
                    JSONArray json = (JSONArray) args[1];

                    addLog(sysMsg);
                    viewersValue.setText(String.valueOf(json.length()));
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String userName = (String) args[0];
                    String message = (String) args[1];

                    removeTyping(userName);
                    filterEmotionCode(userName, message);
                }
            });
        }
    };
}
