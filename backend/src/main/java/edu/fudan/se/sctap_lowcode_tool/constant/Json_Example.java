package edu.fudan.se.sctap_lowcode_tool.constant;

public class Json_Example {
    public static final String json = """
            {
                "trigger": {
                    "event": [
                        {
                            "event_type": "IllegalParking",
                            "params": {
                                "location": "string",
                                "license": "string"
                            }
                        }
                    ]
                },
                "response": {
                    "branch": [
                        {
                            "current_condition": [
                                {
                                    "left": "location.NetworkAudioNum",
                                    "operator": ">",
                                    "right": "0"
                                }
                            ],
                            "chain": [
                                {
                                    "branch": [
                                        {
                                            "history_condition": [
                                                {
                                                    "left": {
                                                        "func": "event_count(IllegalParking, 1, hour)",
                                                        "params": {
                                                            "license": "license"
                                                        }
                                                    },
                                                    "operator": ">",
                                                    "right": "0"
                                                }
                                            ],
                                            "chain": [
                                                {
                                                    "action": {
                                                        "action_name": "IssueWorkOrder",
                                                        "params": {
                                                            "event_type": "IllegalParking",
                                                            "location": "location",
                                                            "data": "Vehicle illegal parking information"
                                                        }
                                                    }
                                                },
                                                {
                                                    "wait": {
                                                        "action_condition": {
                                                            "event_type": "IllegalParking",
                                                            "params": {
                                                                "location": "location"
                                                            }
                                                        }
                                                    }
                                                }
                                            ]
                                        },
                                        {
                                            "history_condition": [
                                                {
                                                    "left": {
                                                        "func": "event_count(IllegalParking, 1, hour)",
                                                        "params": {
                                                            "license": "license"
                                                        }
                                                    },
                                                    "operator": "==",
                                                    "right": "0"
                                                }
                                            ],
                                            "chain": [
                                                {
                                                    "action": {
                                                        "action_name": "Broadcast",
                                                        "params": {
                                                            "event_type": "IllegalParking",
                                                            "location": "location"
                                                        }
                                                    }
                                                },
                                                {
                                                    "wait": {
                                                        "time_condition": {
                                                            "event_type": "IllegalParking",
                                                            "duration": "3",
                                                            "unit": "minute",
                                                            "params": {
                                                                "location": "location"
                                                            }
                                                        }
                                                    }
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            "current_condition": [
                                {
                                    "left": "location.NetworkAudioNum",
                                    "operator": "==",
                                    "right": "0"
                                }
                            ],
                            "chain": [
                                {
                                    "action": {
                                        "action_name": "IssueWorkOrder",
                                        "params": {
                                            "event_type": "IllegalParking",
                                            "location": "location",
                                            "data": "Vehicle illegal parking information"
                                        }
                                    }
                                },
                                {
                                    "wait": {
                                        "action_condition": {
                                            "event_type": "IllegalParking",
                                            "params": {
                                                "location": "location"
                                            }
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            """;
}
