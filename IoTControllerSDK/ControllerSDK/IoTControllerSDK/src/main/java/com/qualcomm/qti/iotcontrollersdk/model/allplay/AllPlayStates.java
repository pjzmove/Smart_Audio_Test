/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;

import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.AudioEffectsState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.BassBoostState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.DolbyState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.DtsVirtaulXState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.EqualizerState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.GroupState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.InputSourceSelectionState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.MediaPlayerState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.MultiChannelGroupMainState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.MultichannelGroupSatellite;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.OutputSourceSelectionState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.PlaylistState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.PresetRevertState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.TrumpetEffectState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.VolumeControlState;

public class AllPlayStates {
  
  private final MediaPlayerState mMediaPlayState = new MediaPlayerState();
  private final GroupState mGroupState = new GroupState();
  private final PlaylistState mPlaylistState = new PlaylistState();
  private final VolumeControlState mVolumeControlState = new VolumeControlState();
  private final VolumeControlState mGroupVolumeControlState = new VolumeControlState();
  private final InputSourceSelectionState mInputSelectorState = new InputSourceSelectionState();
  private final OutputSourceSelectionState mOutputSelectorState = new OutputSourceSelectionState();
  private final BassBoostState mBassBoostState = new BassBoostState();
  private final AudioEffectsState mAudioEffectState = new AudioEffectsState();
  private final PresetRevertState mPresetRevertState = new PresetRevertState();
  private final EqualizerState mEqualizerState = new EqualizerState();
  private final MultiChannelGroupMainState multiChannelGroupMainState = new MultiChannelGroupMainState();
  private final MultichannelGroupSatellite multiChannelGroupSatelliteState = new MultichannelGroupSatellite();
  private final DolbyState mDolbyState = new DolbyState();
  private final DtsVirtaulXState mDtsVirtualXState = new DtsVirtaulXState();
  private final TrumpetEffectState mTrumpetState = new TrumpetEffectState();

  public AllPlayStates() {
  }

  public MediaPlayerState getMediaPlayState() {
      return mMediaPlayState;
  }

  public GroupState getGroupState() {
      return mGroupState;
  }

  public PlaylistState getPlaylistState() {
      return mPlaylistState;
  }

  public VolumeControlState getVolumeControlState() {
      return mVolumeControlState;
  }

  public BassBoostState getBassBoostState() {
      return mBassBoostState;
  }

  public AudioEffectsState getAudioEffectState() {
      return mAudioEffectState;
  }

  public EqualizerState getEqualizerState() {
      return mEqualizerState;
  }

  public PresetRevertState getPresetRevertState() {
      return mPresetRevertState;
  }

  public MultiChannelGroupMainState getMultiChannelGroupMainState() {
      return multiChannelGroupMainState;
  }

  public MultichannelGroupSatellite getMultiChannelGroupSatelliteState() {
      return multiChannelGroupSatelliteState;
  }

  public InputSourceSelectionState getInputSelectorState() {
      return mInputSelectorState;
  }

  public OutputSourceSelectionState getOutputSelectorState() {
      return mOutputSelectorState;
  }

  public DolbyState getDolbyState() {
      return mDolbyState;
  }

  public DtsVirtaulXState getDtsVirtualXState() {
      return mDtsVirtualXState;
  }

  public TrumpetEffectState getTrumpetState() {
    return mTrumpetState;
  }

  public VolumeControlState getGroupVolumeControlState() {
    return mGroupVolumeControlState;
  }

}
