import React, { Component } from 'react';

class LastPlayerActionDialog extends Component {

  constructor(props){
    super(props);
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Last Player's Turn Summary</div>
        <div className="container dialog-container">
          <div style={{margin: "0px 20px"}}>
            <div><u><b>The last player's action was:</b></u></div>
            <div>
                {this.props.lastPlayerName} {this.props.lastPlayerAction}
            </div>
          </div>
          <div className="dialog-button-panel">
            <button className="my-button dialog-button" onClick={this.props.closeFunction}>Close</button>
          </div>
        </div>
      </div>
    );
  }
}

export default LastPlayerActionDialog; 