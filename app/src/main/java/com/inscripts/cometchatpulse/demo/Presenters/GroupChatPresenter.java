package com.inscripts.cometchatpulse.demo.Presenters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cometchat.pro.core.Call;
import com.cometchat.pro.core.MessagesRequest;
import com.cometchat.pro.models.Action;
import com.cometchat.pro.models.User;
import com.inscripts.cometchatpulse.demo.Activity.CometChatActivity;
import com.inscripts.cometchatpulse.demo.Activity.GroupChatActivity;
import com.inscripts.cometchatpulse.demo.Base.Presenter;
import com.inscripts.cometchatpulse.demo.Contracts.GroupChatActivityContract;
import com.inscripts.cometchatpulse.demo.Contracts.StringContract;
import com.inscripts.cometchatpulse.demo.CustomView.CircleImageView;
import com.inscripts.cometchatpulse.demo.R;
import com.inscripts.cometchatpulse.demo.Utils.CommonUtils;
import com.inscripts.cometchatpulse.demo.Utils.Logger;
import com.inscripts.cometchatpulse.demo.Utils.MediaUtils;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.GroupMembersRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.GroupMember;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.TextMessage;


import java.io.File;
import java.util.List;

public class GroupChatPresenter extends Presenter<GroupChatActivityContract.GroupChatView>
        implements GroupChatActivityContract.GroupChatPresenter {

    private Context context;

    private MessagesRequest messagesRequest;

    @Override
    public void getContext(Context context) {

        this.context = context;
    }

    @Override
    public void handleIntent(Intent intent) {

        if (intent.hasExtra(StringContract.IntentStrings.INTENT_GROUP_ID)) {
            String id = CometChat.getLoggedInUser().getUid();
            getBaseView().setOwnerUid(id);
            CometChat.getGroup(intent.getStringExtra(StringContract.IntentStrings.INTENT_GROUP_ID), new CometChat.CallbackListener<Group>() {
                @Override
                public void onSuccess(Group group) {
                    if (isViewAttached())
                        getBaseView().setGroup(group);
                }

                @Override
                public void onError(CometChatException e) {

                }
            });

            getBaseView().setGroupId(intent.getStringExtra(StringContract.IntentStrings.INTENT_GROUP_ID));
        }
        if (intent.hasExtra(StringContract.IntentStrings.INTENT_GROUP_NAME)) {
            if (isViewAttached())
            getBaseView().setTitle(intent.getStringExtra(StringContract.IntentStrings.INTENT_GROUP_NAME));
        }

    }

    @Override
    public void addMessageReceiveListener(String listenerId, final String groupId, final String ownerId) {

        CometChat.addMessageListener(listenerId, new CometChat.MessageListener() {
            @Override
            public void onTextMessageReceived(TextMessage message) {
                if (groupId != null && groupId.equals(message.getReceiverUid()) &&
                        !message.getSender().getUid().equals(ownerId)) {
                    MediaUtils.playSendSound(context,R.raw.receive);
                    getBaseView().addReceivedMessage(message);
                }
            }

            @Override
            public void onMediaMessageReceived(MediaMessage message) {
                if (groupId != null && groupId.equals(message.getReceiverUid()) &&
                        !message.getSender().getUid().equals(ownerId)) {
                    MediaUtils.playSendSound(context,R.raw.receive);
                    getBaseView().addReceivedMessage(message);
                }
            }

        });
    }

    @Override
    public void removeMessageReceiveListener(String ListenerId) {
        try {
            CometChat.removeMessageListener(ListenerId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addGroupEventListener(String listenerId,final String groupId, final String ownerId) {

        CometChat.addGroupListener(listenerId, new CometChat.GroupListener() {
            @Override
            public void onGroupMemberJoined(Action action, User joinedUser, Group joinedGroup) {

                if (groupId != null && groupId.equals(action.getReceiverUid())){
                    if (isViewAttached())
                        getBaseView().addSentMessage(action);
                }
            }

            @Override
            public void onGroupMemberLeft(Action action, User joinedUser, Group joinedGroup) {
                if (groupId != null && groupId.equals(action.getReceiverUid())){
                    if (isViewAttached())
                        getBaseView().addSentMessage(action);
                }
            }

            @Override
            public void onGroupMemberKicked(Action action, User kickedUser, User kickedBy, Group kickedFrom) {
                if (groupId != null && groupId.equals(action.getReceiverUid())){
                    if (isViewAttached())
                        getBaseView().addSentMessage(action);
                }
            }

            @Override
            public void onGroupMemberBanned(Action action, User bannedUser, User bannedBy, Group bannedFrom) {
                if (groupId != null && groupId.equals(action.getReceiverUid())){
                    if (isViewAttached())
                        getBaseView().addSentMessage(action);
                }
            }

            @Override
            public void onGroupMemberUnbanned(Action action, User unbannedUser, User unbannedBy, Group unbannedFrom) {
                if (groupId != null && groupId.equals(action.getReceiverUid())){
                    if (isViewAttached())
                        getBaseView().addSentMessage(action);
                }
            }

            @Override
            public void onGroupMemberScopeChanged(Action action, User user, String scopeChangedTo, String scopeChangedFrom, Group group) {
                if (groupId != null && groupId.equals(action.getReceiverUid())){
                    if (isViewAttached())
                        getBaseView().addSentMessage(action);
                }
            }

        });
    }

    @Override
    public void sendTextMessage(final String message, String groupId) {

        TextMessage textMessage = new TextMessage(groupId, message,
                CometChatConstants.MESSAGE_TYPE_TEXT, CometChatConstants.RECEIVER_TYPE_GROUP);

        CometChat.sendMessage(textMessage, new CometChat.CallbackListener<TextMessage>() {
            @Override
            public void onSuccess(TextMessage textMessage1) {
                MediaUtils.playSendSound(context,R.raw.send);
                if (isViewAttached())
                    getBaseView().addSentMessage(textMessage1);
            }

            @Override
            public void onError(CometChatException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void sendCallRequest(Context Context, String groupId, String receiverType, String callType) {

             Call call = new Call(groupId, receiverType, callType);
             CometChat.initiateCall(call, new CometChat.CallbackListener<Call>() {
                 @Override
                 public void onSuccess(Call call) {
                     CommonUtils.startCallIntent(context, (Group) call.getCallReceiver(), call.getType(), true, call.getSessionId());
                 }

                 @Override
                 public void onError(CometChatException e) {

                 }
             });
    }

    @Override
    public void addCallListener(String call_listener) {
        CometChat.addCallListener(call_listener, new CometChat.CallListener() {
            @Override
            public void onIncomingCallReceived(Call call) {

                    if(call.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {

                        CommonUtils.startCallIntent(context, (User) call.getCallInitiator(), call.getType(),
                                false, call.getSessionId());
                    }
                    else if(call.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {

                        CommonUtils.startCallIntent(context, (Group) call.getCallReceiver(), call.getType(),
                                false, call.getSessionId());
                    }

            }

            @Override
            public void onOutgoingCallAccepted(Call call) {

            }

            @Override
            public void onOutgoingCallRejected(Call call) {

            }

            @Override
            public void onIncomingCallCancelled(Call call) {


            }
        });
    }

    @Override
    public void removeCallListener(String call_listener) {
        CometChat.removeCallListener(call_listener);

    }

    @Override
    public void fetchPreviousMessage(String groupId, int limit) {

        if (messagesRequest == null) {
            messagesRequest = new MessagesRequest.MessagesRequestBuilder().setGUID(groupId).setLimit(limit).build();
            messagesRequest.fetchPrevious(new CometChat.CallbackListener<List<BaseMessage>>() {
                @Override
                public void onSuccess(List<BaseMessage> baseMessages) {
                    for (BaseMessage baseMessage : baseMessages) {
                        Logger.error("groupMessage" + baseMessage.getId() + " timestamp : " + baseMessage.getSentAt() + " list size :" + baseMessages.size());

                    }
                    if (isViewAttached()) {
                        getBaseView().setAdapter(baseMessages);
                    }
                }

                @Override
                public void onError(CometChatException e) {

                }

            });
        } else {
            messagesRequest.fetchPrevious(new CometChat.CallbackListener<List<BaseMessage>>() {
                @Override
                public void onSuccess(List<BaseMessage> baseMessages) {
                    for (BaseMessage baseMessage : baseMessages) {
                        Logger.error("groupMessage" + baseMessage.getId());

                    }

                    if (baseMessages.size() != 0) {
                        if (isViewAttached())
                            getBaseView().setAdapter(baseMessages);
                    }
                }

                @Override
                public void onError(CometChatException e) {

                }

            });
        }

    }

    @Override
    public void fetchGroupMembers(String groupId) {


        GroupMembersRequest groupMembersRequest = new GroupMembersRequest.GroupMembersRequestBuilder(groupId).setLimit(5).build();

        groupMembersRequest.fetchNext(new CometChat.CallbackListener<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                    String s[] = new String[0];
                    if (groupMembers != null && groupMembers.size() != 0) {
                        s= new String[groupMembers.size()];
                        for (int j = 0; j < groupMembers.size(); j++) {

                            s[j] = groupMembers.get(j).getUser().getName();
                        }

                    }
                    if (isViewAttached())
                        getBaseView().setSubTitle(s);

            }

            @Override
            public void onError(CometChatException e) {

            }

        });

    }

    @Override
    public void setGroupIcon(GroupChatActivity groupChatActivity, String icon, CircleImageView groupIcon) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(groupChatActivity.getResources().getDrawable(R.drawable.ic_broken_image));
        Glide.with(groupChatActivity).load(icon).apply(requestOptions).into(groupIcon);

    }

    @Override
    public void sendMediaMessage(File file, String groupId, String messageType) {
        MediaMessage mediaMessage = new MediaMessage(groupId, file, messageType,
                CometChatConstants.RECEIVER_TYPE_GROUP);
        mediaMessage.setSentAt(System.currentTimeMillis());

        CometChat.sendMediaMessage(mediaMessage, new CometChat.CallbackListener<MediaMessage>() {
            @Override
            public void onSuccess(MediaMessage mediaMessage1) {
                if (isViewAttached()) {
                    getBaseView().addSentMessage(mediaMessage1);
                }
                MediaUtils.playSendSound(context,R.raw.send);
            }

            @Override
            public void onError(CometChatException e) {

            }

        });
    }

//

    @Override
    public void leaveGroup(final Group group, final Context context) {

        CometChat.leaveGroup(group.getGuid(), new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                group.setHasJoined(false);
                Toast.makeText(context, "left", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(context,CometChatActivity.class);
                ((Activity)context).startActivityForResult(intent,StringContract.RequestCode.LEFT);
                ((GroupChatActivity)context).finish();
            }

            @Override
            public void onError(CometChatException e) {

            }
        });

    }


}
