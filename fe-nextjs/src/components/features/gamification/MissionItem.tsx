import React from "react";
import { Mission } from "@/types/gamification";
import { CheckCircle2, Zap } from "lucide-react";

interface Props {
  mission: Mission;
}

export default function MissionItem({ mission }: Props) {
  return (
    <div className="flex items-start justify-between p-4 mb-3 bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md transition-all group">
      <div className="flex-1 pr-4">
        <div className="flex items-center gap-2 mb-1">
          <Zap className="w-4 h-4 text-orange-500 fill-orange-500" />
          <h4 className="font-bold text-gray-800 text-sm group-hover:text-blue-600 transition-colors">
            {mission.name}
          </h4>
        </div>
        <p className="text-xs text-gray-500 leading-relaxed mb-2">
          {mission.description}
        </p>
        <div className="inline-flex items-center gap-1 px-2 py-1 bg-gray-50 rounded text-[10px] font-medium text-gray-500 border border-gray-200">
          <CheckCircle2 className="w-3 h-3" />
          Giới hạn: {mission.dailyLimit} lần/ngày
        </div>
      </div>

      <div className="flex flex-col items-center justify-center bg-gradient-to-br from-blue-50 to-blue-100 min-w-[60px] h-[60px] rounded-lg border border-blue-200">
        <span className="text-lg font-black text-blue-600">
          +{mission.points}
        </span>
        <span className="text-[9px] text-blue-500 font-bold uppercase tracking-wider">
          Điểm
        </span>
      </div>
    </div>
  );
}
