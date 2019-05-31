import React, { Component } from 'react';

class PlayerTurnNotificationDialog extends Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Your Turn</div>
        <div className="container dialog-container">
          <div style={{margin: "0px 20px"}}>
            <u><b>It is your turn!</b></u>
          </div>
          <div className="dialog-button-panel">
            <button className="my-button dialog-button" onClick={this.props.closeFunction}>Close</button>
          </div>
        </div>
      </div>
    );
  }
}

export default PlayerTurnNotificationDialog; 