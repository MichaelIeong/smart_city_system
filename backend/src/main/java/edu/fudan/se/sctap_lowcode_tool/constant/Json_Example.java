package edu.fudan.se.sctap_lowcode_tool.constant;

public class Json_Example {
    public static final String json = """
            {
                "trigger": {
                    "event": [
                        {
                            "event_type": "ill_parking",
                            "params": {
                                "location": "string",
                                "plate_number": "string"
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
                                                        "func": "event_count(ill_parking, 1, hour)",
                                                        "params": {
                                                            "plate_number": "plate_number"
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
                                                            "event_type": "ill_parking",
                                                            "location": "location",
                                                            "data": "Vehicle illegal parking information"
                                                        }
                                                    }
                                                },
                                                {
                                                    "wait": {
                                                        "action_condition": {
                                                            "event_type": "ill_parking",
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
                                                        "func": "event_count(ill_parking, 1, hour)",
                                                        "params": {
                                                            "plate_number": "plate_number"
                                                        }
                                                    },
                                                    "operator": "=",
                                                    "right": "0"
                                                }
                                            ],
                                            "chain": [
                                                {
                                                    "action": {
                                                        "action_name": "Broadcast",
                                                        "params": {
                                                            "event_type": "ill_parking",
                                                            "location": "location"
                                                        }
                                                    }
                                                },
                                                {
                                                    "wait": {
                                                        "time_condition": {
                                                            "event_type": "ill_parking",
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
                                    "operator": "=",
                                    "right": "0"
                                }
                            ],
                            "chain": [
                                {
                                    "action": {
                                        "action_name": "IssueWorkOrder",
                                        "params": {
                                            "event_type": "ill_parking",
                                            "location": "location",
                                            "data": "Vehicle illegal parking information"
                                        }
                                    }
                                },
                                {
                                    "wait": {
                                        "action_condition": {
                                            "event_type": "ill_parking",
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
