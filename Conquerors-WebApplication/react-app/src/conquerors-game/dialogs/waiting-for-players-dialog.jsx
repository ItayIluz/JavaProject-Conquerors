import React, { Component } from 'react';

class WaitingForPlayersDialog extends Component {

  constructor(props){
    super(props);
    this.state = {
      animationDots: "",
      numberOfDots: 0
    }

    this.waitingAnimationInterval = null;
    this.animationDots = 0;
    this.createWaitingAnimation = this.createWaitingAnimation.bind(this);
  }

  componentDidMount() {
    this.createWaitingAnimation();
    this.waitingAnimationInterval = setInterval(this.createWaitingAnimation, 500);
  }

  createWaitingAnimation(){
    let newDots = this.state.animationDots;
    let numberOfDots = this.state.numberOfDots;

    if(this.state.numberOfDots == 3){
      newDots = "";
      numberOfDots = 0;
    } else {
      newDots += ".";
      numberOfDots++;
    }

    this.setState({
      animationDots: newDots,
      numberOfDots: numberOfDots
    });
  }

  componentWillUnmount(){
    clearInterval(this.waitingAnimationInterval);
  }

  render() {
    return (
      <div className="container-header dialog-header">
        <div className="container-header-title">Waiting For Players...</div>
        <div className="container dialog-container">
          <div style={{margin: "0px 20px"}}>
            <u><b>
              {!this.props.allPlayersAreIn ?
                "Waiting for all players to join the game." : 
                "All players are in!"
              }
            </b></u>
            <div style={{"marginTop": "10px"}}><b>{!this.props.allPlayersAreIn ?
                "Please wait" : 
                "The game will start shortly"
              }<span>{this.state.animationDots}</span></b></div>
          </div>
          <div className="dialog-button-panel">
            <button className="my-button dialog-button" onClick={this.props.leaveGameFunction}>Leave Game</button>
          </div>
        </div>
      </div>
    );
  }
}

export default WaitingForPlayersDialog; 